const USER_LOCATION_SCOPE = 'scope.userLocation'

async function requestLocationSettings () {
  const confirmation = await invokeUni('showModal', {
    title: '开启定位权限',
    content: '定位用于获取您所在城市并展示当地服务，请在设置中允许使用位置信息。',
    confirmText: '去设置',
    cancelText: '暂不开启'
  })
  if (!confirmation?.confirm) throw new Error('用户暂未开启定位权限')
  const setting = await invokeUni('openSetting')
  if (!setting?.authSetting?.[USER_LOCATION_SCOPE]) {
    throw new Error('定位权限未开启，请稍后重试')
  }
}

function invokeUni (method, options = {}) {
  return new Promise((resolve, reject) => {
    uni[method]({
      ...options,
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 获取当前设备坐标。微信小程序首次使用时申请权限，曾拒绝时引导到设置页。
 * 使用 WGS84 坐标供天地图逆地理编码服务解析。
 */
export async function getCurrentCoordinates () {
  const isWechatMiniProgram = typeof wx !== 'undefined' && typeof wx.getSetting === 'function'

  if (isWechatMiniProgram && typeof uni.getSetting === 'function') {
    const setting = await invokeUni('getSetting')
    const granted = setting?.authSetting?.[USER_LOCATION_SCOPE]

    if (granted === false) {
      await requestLocationSettings()
    } else if (granted !== true) {
      try {
        await invokeUni('authorize', { scope: USER_LOCATION_SCOPE })
      } catch {
        await requestLocationSettings()
      }
    }
  }

  const location = await invokeUni('getLocation', { type: 'wgs84' })
  return {
    longitude: location.longitude,
    latitude: location.latitude
  }
}
