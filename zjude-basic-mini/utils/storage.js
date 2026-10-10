export function getStorage (key, fallback = null) {
  try {
    const value = uni.getStorageSync(key)
    return value === '' || value === undefined ? fallback : value
  } catch (error) {
    console.warn(`[storage] failed to read ${key}`, error)
    return fallback
  }
}

export function setStorage (key, value) {
  try {
    uni.setStorageSync(key, value)
  } catch (error) {
    console.warn(`[storage] failed to write ${key}`, error)
  }
}

export function removeStorage (key) {
  try {
    uni.removeStorageSync(key)
  } catch (error) {
    console.warn(`[storage] failed to remove ${key}`, error)
  }
}
