import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import DictTag from './index.vue'

const options = [
  { label: '已启用', value: '1', elTagType: 'success', elTagClass: '' },
  { label: '已停用', value: '0', elTagType: 'danger', elTagClass: '' }
]

function mountDictTag (props) {
  return mount(DictTag, {
    props,
    global: {
      stubs: {
        ElTag: {
          template: '<span class="el-tag-stub"><slot /></span>'
        }
      }
    }
  })
}

describe('DictTag', () => {
  it('renders labels for matching values', () => {
    const wrapper = mountDictTag({ options, value: ['1', '0'] })

    expect(wrapper.text()).toContain('已启用')
    expect(wrapper.text()).toContain('已停用')
  })

  it('renders unmatched values when enabled', () => {
    const wrapper = mountDictTag({ options, value: 'unknown', showValue: true })

    expect(wrapper.text()).toContain('unknown')
  })
})
