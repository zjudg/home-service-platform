<template>
  <div
    class="wang-editor"
    :style="{ '--wang-editor-height': `${editorHeight}px` }"
  >
    <Toolbar
      class="wang-editor__toolbar"
      :editor="editorRef"
      :default-config="toolbarConfig"
      :mode="mode"
    />
    <Editor
      :model-value="modelValue || ''"
      :default-config="editorConfig"
      :mode="mode"
      @on-created="handleCreated"
      @update:model-value="handleUpdate"
    />
  </div>
</template>

<script setup name="WangEditor">
import { computed, nextTick, onBeforeUnmount, shallowRef, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { DomEditor, SlateEditor, SlateElement, SlateTransforms } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  height: {
    type: Number,
    default: 360
  },
  // 兼容原有页面使用的 :min-height 属性。
  minHeight: {
    type: Number,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  autoFocus: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '请输入内容'
  },
  mode: {
    type: String,
    default: 'default'
  },
  maxImageSize: {
    type: Number,
    default: 10 * 1024 * 1024
  }
})

const emit = defineEmits(['update:modelValue'])
const editorRef = shallowRef()
const editorHeight = computed(() => props.minHeight || props.height)
const toolbarConfig = {
  // Keep only local upload. The network-image dialog is not included.
  excludeKeys: ['insertImage']
}
let lastEmittedValue = props.modelValue || ''

async function uploadImage (file) {
  const isImage = /\.(bmp|gif|jpe?g|png)$/i.test(file?.name || '')
  if (!isImage) {
    ElMessage.error('只支持上传图片文件')
    return null
  }
  if (file.size > props.maxImageSize) {
    ElMessage.error(`图片大小不能超过 ${Math.round(props.maxImageSize / 1024 / 1024)} MB`)
    return null
  }

  const formData = new FormData()
  formData.append('file', file)
  try {
    const result = await request({
      url: '/common/upload',
      method: 'post',
      data: formData,
      timeout: 120000,
      // Override the global JSON default during Axios request transformation.
      // Axios removes this header in browsers so the browser can add the
      // multipart boundary for FormData.
      headers: {
        'Content-Type': 'multipart/form-data',
        repeatSubmit: false
      }
    })
    const imageUrl = result?.url || result?.fileName
    if (!imageUrl) {
      ElMessage.error('上传接口未返回图片地址')
      return null
    }
    return imageUrl
  } catch {
    // The shared request wrapper already displays the server/network error.
    return null
  }
}

function insertUploadedImage (imageUrl, fileName) {
  const editor = editorRef.value
  if (!editor || editor.isDestroyed) {
    ElMessage.error('编辑器已关闭，请重新打开后上传图片')
    return
  }

  focusEditorAtImageInsertionPoint(editor)
  SlateTransforms.insertNodes(editor, {
    type: 'image',
    src: imageUrl,
    href: '',
    alt: fileName,
    style: {},
    children: [{ text: '' }]
  })
}

async function uploadAndInsertImage (file, insertFn) {
  const imageUrl = await uploadImage(file)
  if (imageUrl) {
    focusEditorAtImageInsertionPoint(editorRef.value)
    insertFn(imageUrl, file.name, '')
  }
}

function openLocalImagePicker (onUploaded = insertUploadedImage) {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.bmp,.gif,.jpg,.jpeg,.png,image/bmp,image/gif,image/jpeg,image/png'
  input.hidden = true
  document.body.appendChild(input)

  const handlePickerClose = () => {
    // Some browsers do not dispatch `change` when the user cancels selection.
    window.removeEventListener('focus', handlePickerClose)
    window.setTimeout(() => input.remove(), 1000)
  }
  const handleFileChange = async () => {
    const file = input.files?.[0]
    window.removeEventListener('focus', handlePickerClose)
    input.remove()
    if (!file) return
    const imageUrl = await uploadImage(file)
    if (imageUrl) onUploaded(imageUrl, file.name)
  }

  input.addEventListener('change', handleFileChange, { once: true })
  window.addEventListener('focus', handlePickerClose, { once: true })
  input.click()
}

function browseAndUploadImage (insertFn) {
  openLocalImagePicker((imageUrl, fileName) => {
    focusEditorAtImageInsertionPoint(editorRef.value)
    insertFn(imageUrl, fileName, '')
  })
}

const editorConfig = {
  placeholder: props.placeholder,
  MENU_CONF: {
    uploadImage: {
      customUpload: uploadAndInsertImage,
      customBrowseAndUpload: browseAndUploadImage,
      maxFileSize: props.maxImageSize,
      allowedFileTypes: ['image/*']
    }
  }
}

function handleCreated (editor) {
  editorRef.value = editor
  if (props.disabled) {
    editor.disable()
  } else if (props.autoFocus) {
    focusEditorAtImageInsertionPoint(editor)
  }
  // Local upload should remain available even before content has a selection
  // or when the caret is inside a heading/list/code block.
  nextTick(() => configureUploadMenu(editor))
}

function configureUploadMenu (editor, retry = 0) {
  if (!editor || editor.isDestroyed) return
  const toolbar = DomEditor.getToolbar(editor)
  const uploadMenu = toolbar?.getMenus()?.uploadImage
  if (!uploadMenu) {
    if (retry < 5) window.setTimeout(() => configureUploadMenu(editor, retry + 1), 50)
    return
  }

  // alwaysEnable bypasses wangEditor toolbar's null-selection/read-only gate.
  // The actual disabled prop is still respected by the upload callback.
  uploadMenu.alwaysEnable = !props.disabled
  uploadMenu.isDisabled = () => props.disabled
  toolbar.changeToolbarState()
}

function focusEditorAtImageInsertionPoint (editor) {
  if (!editor || editor.isDestroyed || props.disabled || !editor.children?.length) return

  // wangEditor disables image menus in list items, headings, quotes, and code
  // blocks. Pick a top-level paragraph instead of blindly focusing the last
  // node (service descriptions commonly end with a bulleted list).
  const paragraphs = Array.from(SlateEditor.nodes(editor, {
    at: [],
    match: node => SlateElement.isElement(node) && node.type === 'paragraph',
    mode: 'highest'
  }))
  const paragraph = paragraphs.reverse().find(([, path]) => path.length === 1)
  if (paragraph) {
    editor.select(SlateEditor.end(editor, paragraph[1]))
  } else {
    const insertionPath = [editor.children.length]
    SlateTransforms.insertNodes(editor, { type: 'paragraph', children: [{ text: '' }] }, { at: insertionPath })
    editor.select(SlateEditor.end(editor, [editor.children.length - 1]))
  }
  editor.focus()
}

function handleUpdate (value) {
  lastEmittedValue = value
  emit('update:modelValue', value)
}

watch(() => props.modelValue, async value => {
  if (value === lastEmittedValue) return
  lastEmittedValue = value || ''
  if (!props.autoFocus) return
  await nextTick()
  const editor = editorRef.value
  focusEditorAtImageInsertionPoint(editor)
})

watch(() => props.disabled, disabled => {
  if (!editorRef.value) return
  if (disabled) {
    editorRef.value.disable()
  } else {
    editorRef.value.enable()
  }
  configureUploadMenu(editorRef.value)
})

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor && !editor.isDestroyed) {
    editor.destroy()
  }
  editorRef.value = null
})
</script>

<style scoped lang="scss">
.wang-editor {
  width: 100%;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;

  :deep(.wang-editor__toolbar) {
    border-bottom: 1px solid #e4e7ed !important;
  }

  :deep(.w-e-text-container) {
    height: var(--wang-editor-height) !important;
  }

  :deep(.w-e-text-placeholder) {
    color: #a8abb2;
  }
}
</style>
