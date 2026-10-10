import request from '@/utils/request'

function resourceApi (type) {
  const base = `/mall/content/${type}`
  return {
    list: query => request({ url: `${base}/list`, method: 'get', params: query }),
    get: id => request({ url: `${base}/${id}`, method: 'get' }),
    add: data => request({ url: base, method: 'post', data }),
    update: data => request({ url: base, method: 'put', data }),
    remove: id => request({ url: `${base}/${id}`, method: 'delete' })
  }
}

export const contentApis = {
  ad: resourceApi('AD'),
  slot: resourceApi('SLOT'),
  help: resourceApi('HELP'),
  helpCategory: resourceApi('HELP_CAT'),
  article: resourceApi('ARTICLE'),
  articleCategory: resourceApi('ARTICLE_CAT')
}
