"""Seed the local MySQL instance with nationwide city data and runnable home-service data."""
import json
import os
import urllib.request

import pymysql
from pypinyin import lazy_pinyin


DB_NAME = os.getenv("HOME_SERVICE_DB", "home-service-system")
DB_PASSWORD = os.getenv("HOME_SERVICE_DB_PASSWORD")
if not DB_PASSWORD:
    raise SystemExit("HOME_SERVICE_DB_PASSWORD is required")


def fetch_json(url):
    with urllib.request.urlopen(url, timeout=30) as response:
        return json.load(response)


def main():
    provinces = fetch_json("https://unpkg.com/china-division@2.4.0/dist/provinces.json")
    cities = fetch_json("https://unpkg.com/china-division@2.4.0/dist/cities.json")
    province_map = {str(item["code"]): item["name"] for item in provinces}
    connection = pymysql.connect(host="127.0.0.1", port=3306, user="root", password=DB_PASSWORD, database=DB_NAME, charset="utf8mb4")
    try:
        with connection.cursor() as cursor:
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS zjudg_city (
                    city_id varchar(16) NOT NULL PRIMARY KEY,
                    city_name varchar(64) NOT NULL,
                    province_name varchar(64) NOT NULL,
                    initial_letter char(1) NOT NULL,
                    status char(1) NOT NULL DEFAULT '0',
                    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time datetime NULL,
                    KEY idx_zjudg_city_initial (initial_letter, city_name),
                    KEY idx_zjudg_city_province (province_name, city_name)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """)
            rows = []
            for item in cities:
                name = item["name"]
                pinyin = "".join(lazy_pinyin(name))
                rows.append((str(item["code"]), name, province_map.get(str(item["provinceCode"]), ""), (pinyin[:1] or "#").upper(), "0"))
            cursor.executemany("""
                INSERT INTO zjudg_city (city_id, city_name, province_name, initial_letter, status)
                VALUES (%s, %s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE city_name=VALUES(city_name), province_name=VALUES(province_name), initial_letter=VALUES(initial_letter), status='0'
            """, rows)
            cursor.execute("""
                INSERT IGNORE INTO zjudg_service_category (category_id,parent_id,category_name,image_url,sort_num,home_recommend,status,create_by,create_time) VALUES
                ('ZC_CLEAN','0','保洁清洗','',1,'1','0','system',NOW()),('ZC_REPAIR','0','家电维修','',2,'1','0','system',NOW()),
                ('ZC_INSTALL','0','上门安装','',3,'1','0','system',NOW()),('ZC_NANNY','0','母婴护理','',4,'1','0','system',NOW()),('ZC_CARE','0','老人陪护','',5,'1','0','system',NOW())
            """)
            cursor.execute("""
                INSERT IGNORE INTO zjudg_service_unit (unit_id,unit_name,sort_num,status,create_by,create_time) VALUES
                ('ZU_HOUR','小时',1,'0','system',NOW()),('ZU_TIME','次',2,'0','system',NOW()),('ZU_MONTH','月',3,'0','system',NOW())
            """)
            services = [
                ("ZS_CLEAN_DAILY", "日常保洁", "厨房、卫生间、客厅全屋日常清洁", "ZC_CLEAN", 39, 59, "ZU_HOUR", 1),
                ("ZS_CLEAN_DEEP", "深度保洁", "针对重点区域的深度清洁", "ZC_CLEAN", 199, 259, "ZU_TIME", 2),
                ("ZS_REPAIR_AC", "空调维修", "空调故障检测与上门维修", "ZC_REPAIR", 80, 100, "ZU_TIME", 3),
                ("ZS_INSTALL_WATER", "热水器安装", "热水器及配件上门安装", "ZC_INSTALL", 220, 280, "ZU_TIME", 4),
                ("ZS_NANNY_MATERNITY", "月嫂服务", "持证月嫂提供产后母婴照护", "ZC_NANNY", 6800, 7800, "ZU_MONTH", 5),
                ("ZS_CARE_ELDER", "老人陪护", "居家老人日常陪护与生活照料", "ZC_CARE", 180, 220, "ZU_TIME", 6),
            ]
            cursor.executemany("""
                INSERT IGNORE INTO zjudg_service (service_id,service_name,service_intro,category_id,carousel_images,price,original_price,unit_id,sort_num,status,detail_content,create_by,create_time)
                VALUES (%s,%s,%s,%s,'',%s,%s,%s,%s,'0',%s,'system',NOW())
            """, [(sid, name, intro, category, price, original, unit, sort_num, intro) for sid, name, intro, category, price, original, unit, sort_num in services])
            cursor.execute("""
                INSERT INTO zjudg_decoration_config (config_id,config_type,config_json,update_time) VALUES
                ('DEC_HOME_DEFAULT','home','[{"id":"home-clean","title":"安心到家","subtitle":"专业家政服务，提前预约更省心","image":"","serviceId":"ZS_CLEAN_DAILY"}]',NOW())
                ON DUPLICATE KEY UPDATE config_json=VALUES(config_json), update_time=NOW()
            """)
            configs = [
                ('网站名称', 'website.shopName', '妙洁家政', '家政小程序站点名称'),
                ('首页轮播图', 'decorate.home.banners', '[{"id":"home-clean","title":"安心到家","subtitle":"专业家政服务，提前预约更省心","image":"","serviceId":"ZS_CLEAN_DAILY"}]', '首页由后台维护的轮播配置'),
                ('首页搜索提示', 'decorate.home.searchPlaceholder', '请输入服务名称，如日常保洁', '首页搜索框提示'),
            ]
            for name, key, value, remark in configs:
                cursor.execute('UPDATE sys_config SET config_name=%s, config_value=%s, config_type=\'N\', update_time=NOW(), remark=%s WHERE config_key=%s', (name, value, remark, key))
                if cursor.rowcount == 0:
                    cursor.execute('INSERT INTO sys_config (config_name,config_key,config_value,config_type,create_by,create_time,remark) VALUES (%s,%s,%s,\'N\',\'system\',NOW(),%s)', (name, key, value, remark))
        connection.commit()
        print(f"seeded {len(rows)} cities and {len(services)} services into {DB_NAME}")
    finally:
        connection.close()


if __name__ == "__main__":
    main()
