FROM dragonwell-registry.cn-hangzhou.cr.aliyuncs.com/dragonwell/dragonwell:21

VOLUME /tmp

ADD start.sh start.sh

ADD target/*.jar app.jar

RUN echo "Asia/Shanghai" > /etc/timezone

RUN mkdir /files -p

RUN chmod +x start.sh

RUN chmod +x /files

CMD ["sh", "start.sh"]