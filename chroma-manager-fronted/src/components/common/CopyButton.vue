<template>
  <el-tooltip :content="tooltipContent" placement="top" :open-delay="100">
    <el-button 
      class="copy-button" 
      type="text" 
      size="mini" 
      @click="copyToClipboard"
      :disabled="!text"
    >
      <i :class="iconClass"></i>
    </el-button>
  </el-tooltip>
</template>

<script>
export default {
  name: 'CopyButton',
  props: {
    text: {
      type: [String, Number, Object, Array],
      required: true,
      default: ''
    },
    formatter: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      copied: false,
      copyTimeout: null
    }
  },
  computed: {
    formattedText() {
      if (!this.text) return '';
      
      if (this.formatter) {
        return this.formatter(this.text);
      }
      
      if (typeof this.text === 'object') {
        return JSON.stringify(this.text);
      }
      
      return String(this.text);
    },
    tooltipContent() {
      return this.copied ? '已复制!' : '复制';
    },
    iconClass() {
      return this.copied ? 'el-icon-check' : 'el-icon-document-copy';
    }
  },
  methods: {
    async copyToClipboard() {
      if (!this.formattedText) return;
      
      try {
        // 使用现代的Clipboard API，更适合处理大数据量
        if (navigator.clipboard && navigator.clipboard.writeText) {
          await navigator.clipboard.writeText(this.formattedText);
          this.copied = true;
          this.$emit('copy-success', this.formattedText);
        } else {
          // 降级方案，使用旧方法
          const textArea = document.createElement('textarea');
          textArea.value = this.formattedText;
          textArea.style.position = 'fixed';
          textArea.style.left = '-999999px';
          textArea.style.top = '-999999px';
          document.body.appendChild(textArea);
          textArea.focus();
          textArea.select();
          
          const successful = document.execCommand('copy');
          document.body.removeChild(textArea);
          
          if (successful) {
            this.copied = true;
            this.$emit('copy-success', this.formattedText);
          } else {
            this.$emit('copy-error', new Error('复制失败'));
            return;
          }
        }
        
        // 重置复制状态
        clearTimeout(this.copyTimeout);
        this.copyTimeout = setTimeout(() => {
          this.copied = false;
        }, 2000);
      } catch (err) {
        console.error('复制失败:', err);
        this.$emit('copy-error', err);
      }
    }
  },
  beforeDestroy() {
    clearTimeout(this.copyTimeout);
  }
}
</script>

<style scoped>
.copy-button {
  margin-left: 5px;
  padding: 2px;
}

.copy-button i {
  font-size: 14px;
}
</style>