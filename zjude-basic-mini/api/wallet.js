import { request } from '@/utils/request'

// 师傅钱包与提现：金额对外统一以元字符串返回/提交
export const getWallet = () => request({ url: '/mini/master/wallet', method: 'GET' })

export const listWalletLogs = (params = {}) =>
  request({ url: '/mini/master/wallet/logs', method: 'GET', data: params })

export const applyWithdraw = (amountYuan, payAccount) =>
  request({ url: '/mini/master/wallet/withdraw', method: 'POST', data: { amountYuan, payAccount } })

export const listMyWithdraws = () => request({ url: '/mini/master/wallet/withdraws', method: 'GET' })
