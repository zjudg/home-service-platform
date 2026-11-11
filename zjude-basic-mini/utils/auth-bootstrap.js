/** Restore a saved session or silently sign in an existing WeChat user. */
export async function bootstrapAuthSession ({ authStore, isWeixin, login }) {
  const restored = await authStore.restore()
  if (restored || authStore.isLoggedIn || !isWeixin) return Boolean(authStore.isLoggedIn)

  try {
    const result = await login()
    if (!result?.code) return false

    const signInResult = await authStore.silentWechatSignIn(result.code)
    return signInResult?.registered !== false && Boolean(authStore.isLoggedIn)
  } catch (error) {
    // A new user or temporary WeChat/network failure remains a guest.
    return false
  }
}
