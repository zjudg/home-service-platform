import hasRole from './permission/hasRole'
import hasPerm from './permission/hasPerm'
import copyText from './common/copyText'

export default function directive (app) {
  app.directive('hasRole', hasRole)
  app.directive('hasPerm', hasPerm)
  app.directive('copyText', copyText)
}
