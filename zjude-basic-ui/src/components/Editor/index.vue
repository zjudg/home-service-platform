<template>
  <div class="tinymce-box">
    <editor
      v-model="textContent"
      :init="init"
      :disabled="disabled"
    ></editor>
  </div>
</template>

<script setup name="Tinymce">
import Editor from '@tinymce/tinymce-vue'
import tinymce from 'tinymce/tinymce'
import 'tinymce/themes/silver/theme' // 主题文件
import 'tinymce/icons/default'
import 'tinymce/models/dom'
// tinymce插件可按自己的需要进行导�?// 更多插件参考：https://www.tiny.cloud/docs/plugins/
import 'tinymce/plugins/image' // 插入上传图片插件
import 'tinymce/plugins/importcss' // 图片工具
import 'tinymce/plugins/media' // 插入视频插件
import 'tinymce/plugins/table' // 插入表格插件
import 'tinymce/plugins/lists' // 列表插件
import 'tinymce/plugins/charmap' // 特殊字符
import 'tinymce/plugins/wordcount' // 字数统计插件
import 'tinymce/plugins/codesample' // 插入代码
import 'tinymce/plugins/code' // 查看源码
import 'tinymce/plugins/fullscreen' // 全屏
import 'tinymce/plugins/link' //
import 'tinymce/plugins/preview' // 预览
import 'tinymce/plugins/save' // 保存
import 'tinymce/plugins/searchreplace' // 查询替换
import 'tinymce/plugins/pagebreak' // 分页
import 'tinymce/plugins/insertdatetime' // 时间插入

// Register the TinyMCE core singleton before theme and plugin modules run.
window.tinymce = tinymce

const props = defineProps({
  modelValue: String,
  disabled: {
    type: Boolean,
    default: false
  },
  height: {
    type: Number,
    default: 400
  },
  plugins: {
    type: [String, Array],
    default: 'lists image media table wordcount save preview'
    // 插件需要import进来
    // default: 'wordcount visualchars visualblocks toc textpattern template tabfocus spellchecker searchreplace save quickbars print preview paste pagebreak noneditable nonbreaking media insertdatetime importcss imagetools image hr help fullscreen fullpage directionality codesample code charmap link code table lists advlist anchor autolink autoresize autosave'
  },
  toolbar: {
    type: [String, Array],
    default: 'formats undo redo paste print fontsizeselect fontselect template fullpage|wordcount ltr rtl visualchars visualblocks toc spellchecker searchreplace|save preview pagebreak nonbreaking|media image|outdent indent aligncenter alignleft alignright alignjustify lineheight  underline quicklink h2 h3 blockquote numlist bullist table removeformat forecolor backcolor bold italic  strikethrough hr charmap link insertdatetime|subscript superscript cut codesample code |anchor preview fullscreen|help'
  }
})

const init = reactive({
  height: props.height,
  language_url: '/tinymce/langs/zh-Hans.js',
  language: 'zh-Hans',
  // 皮肤：这里引入的是public下的资源文件
  skin_url: '/tinymce/skins/ui/oxide',
  // skin_url: 'tinymce/skins/ui/oxide-dark',//黑色�?  plugins: props.plugins,
  toolbar: props.toolbar,
  branding: false,
  // 隐藏菜单�?  menubar: false,
  // 是否显示底部状态栏
  statusbar: true,
  // convert_urls: false,
  // 初始化完�?  // init_instance_callback: (editor) => {
  //   console.log("初始化完成：",editor)
  // },
  // 此处为图片上传处理函数，这个直接用了base64的图片形式上传图片，
  // 如需ajax上传可参考https://www.tiny.cloud/docs/configure/file-image-upload/#images_upload_handler
  images_upload_handler: (blobInfo) => {
    return new Promise((resolve, reject) => {
      try {
        const img = 'data:' + blobInfo.blob().type + ';base64,' + blobInfo.base64()
        resolve(img)
      } catch (error) {
        reject(error)
      }
    })
  }
})
const textContent = ref(props.modelValue)

const clear = () => {
  textContent.value = ''
}
// 暴露方法供父组件调用
defineExpose({ clear })
</script>

<style scoped>
.tinymce-box{
  margin: 20px;
}
</style>
