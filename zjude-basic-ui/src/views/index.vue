<template>
  <div class="app-container home">
    <section class="workspace-hero">
      <div class="hero-copy">
        <div class="eyebrow">ZJUDE / ENGINEERING WORKSPACE</div>
        <h1>欢迎进入 <strong>Zjude 工作台</strong></h1>
        <p>研发协作、平台能力与业务运行状态，正在这里持续运转。</p>
        <div class="hero-status"><i /> SYSTEM READY <span>·</span> ALL CORE SERVICES ONLINE</div>
      </div>
      <div class="hero-visual" aria-hidden="true">
        <div class="orbit orbit-one" />
        <div class="orbit orbit-two" />
        <div class="core-mark">J</div>
        <span class="node node-one" />
        <span class="node node-two" />
        <span class="node node-three" />
      </div>
    </section>

    <section class="status-grid">
      <article class="status-card">
        <span class="card-label">CORE SERVICES</span>
        <strong>ONLINE</strong>
        <small>全部服务运行正常</small>
        <i class="status-dot" />
      </article>
      <article class="status-card">
        <span class="card-label">ACTIVE MODULES</span>
        <strong>08</strong>
        <small>系统模块已加载</small>
        <i class="status-dot" />
      </article>
      <article class="status-card">
        <span class="card-label">WORKSPACE</span>
        <strong>READY</strong>
        <small>等待你的下一项任务</small>
        <i class="status-dot" />
      </article>
    </section>

    <section class="module-grid">
      <article class="module-card">
        <span class="module-index">01</span>
        <div>
          <h2>研发协作</h2>
          <p>连接团队、项目与交付流程。</p>
        </div>
        <span class="module-arrow">↗</span>
      </article>
      <article class="module-card">
        <span class="module-index">02</span>
        <div>
          <h2>平台服务</h2>
          <p>统一管理系统基础能力。</p>
        </div>
        <span class="module-arrow">↗</span>
      </article>
      <article class="module-card">
        <span class="module-index">03</span>
        <div>
          <h2>运行监控</h2>
          <p>掌握服务状态与关键指标。</p>
        </div>
        <span class="module-arrow">↗</span>
      </article>
    </section>
  </div>
</template>

<script setup name="Index">
</script>

<style scoped lang="scss">
.home {
  min-height: calc(100vh - 84px);
  position: relative;
  overflow: hidden;
  padding: 52px clamp(28px, 5vw, 82px) 72px;
  color: #dbeafe;
  background:
    linear-gradient(135deg, rgba(14, 165, 233, 0.08), transparent 38%),
    linear-gradient(315deg, rgba(37, 99, 235, 0.13), transparent 50%),
    #071d2d;
}

.home::before {
  content: "";
  position: absolute;
  inset: 0;
  opacity: 0.24;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(125, 211, 252, 0.09) 1px, transparent 1px),
    linear-gradient(90deg, rgba(125, 211, 252, 0.09) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: linear-gradient(to bottom, black, transparent 90%);
}

.workspace-hero,
.status-grid,
.module-grid {
  position: relative;
  z-index: 1;
  max-width: 1180px;
  margin: 0 auto;
}

.workspace-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 300px;
  border-bottom: 1px solid rgba(125, 211, 252, 0.16);
}

.eyebrow,
.card-label {
  color: #67e8f9;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
}

.hero-copy h1 {
  margin: 22px 0 14px;
  color: #f8fafc;
  font-size: clamp(34px, 5vw, 60px);
  line-height: 1.08;
}

.hero-copy h1 strong {
  color: #67e8f9;
  font-weight: 700;
}

.hero-copy p {
  margin: 0;
  color: rgba(219, 234, 254, 0.68);
  font-size: 15px;
}

.hero-status {
  display: flex;
  align-items: center;
  gap: 9px;
  margin-top: 28px;
  color: rgba(186, 230, 253, 0.62);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.hero-status i,
.status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22d3ee;
  box-shadow: 0 0 14px #22d3ee;
}

.hero-status span {
  color: rgba(186, 230, 253, 0.3);
}

.hero-visual {
  position: relative;
  width: 270px;
  height: 220px;
  margin-right: 5%;
}

.orbit {
  position: absolute;
  inset: 26px 0;
  border: 1px solid rgba(103, 232, 249, 0.3);
  transform: rotate(-24deg);
}

.orbit-one {
  border-radius: 50%;
}

.orbit-two {
  border-radius: 50%;
  transform: rotate(58deg) scale(0.72);
}

.core-mark {
  position: absolute;
  top: 78px;
  left: 108px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border: 1px solid rgba(125, 211, 252, 0.5);
  border-radius: 10px;
  color: white;
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
  box-shadow: 0 0 30px rgba(34, 211, 238, 0.35);
  font-size: 24px;
  font-weight: 700;
}

.node {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #67e8f9;
  box-shadow: 0 0 14px #22d3ee;
}

.node-one { top: 24px; left: 86px; }
.node-two { right: 10px; top: 102px; }
.node-three { bottom: 24px; left: 60px; }

.status-grid,
.module-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.status-grid {
  margin-top: 26px;
}

.status-card,
.module-card {
  position: relative;
  border: 1px solid rgba(125, 211, 252, 0.17);
  border-radius: 8px;
  background: rgba(7, 35, 56, 0.66);
}

.status-card {
  min-height: 112px;
  padding: 20px 22px;
}

.status-card strong {
  display: block;
  margin-top: 13px;
  color: #f0f9ff;
  font-size: 22px;
  letter-spacing: 0.08em;
}

.status-card small {
  display: block;
  margin-top: 7px;
  color: rgba(186, 230, 253, 0.55);
  font-size: 12px;
}

.status-dot {
  position: absolute;
  top: 23px;
  right: 23px;
}

.module-grid {
  margin-top: 14px;
}

.module-card {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  min-height: 112px;
  padding: 22px;
  transition: border-color 0.2s, background 0.2s, transform 0.2s;
}

.module-card:hover {
  border-color: rgba(103, 232, 249, 0.55);
  background: rgba(8, 48, 73, 0.85);
  transform: translateY(-2px);
}

.module-index {
  color: #38bdf8;
  font: 700 11px/1 Arial, sans-serif;
  letter-spacing: 0.08em;
}

.module-card h2 {
  margin: 0;
  color: #f0f9ff;
  font-size: 17px;
}

.module-card p {
  margin: 9px 0 0;
  color: rgba(186, 230, 253, 0.56);
  font-size: 12px;
}

.module-arrow {
  margin-left: auto;
  color: #67e8f9;
  font-size: 20px;
}

@media (max-width: 760px) {
  .home {
    padding: 36px 20px 52px;
  }

  .workspace-hero {
    min-height: 0;
    padding-bottom: 34px;
  }

  .hero-visual {
    display: none;
  }

  .status-grid,
  .module-grid {
    grid-template-columns: 1fr;
  }
}
</style>
