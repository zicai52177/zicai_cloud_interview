/// <reference types="vite/client" />

declare module '*.css' {
  const content: Record<string, string>
  export default content
}

declare module 'element-plus/dist/index.css' {
  const content: string
  export default content
}