/* ==========================================================================
   DevPulse - App Details & Download Interactive Engine
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
  initNavbarScroll();
  initThemeToggle();
  initCopyChecksum();
  initScreenshotLightbox();
});

/* Navbar Scroll Shadow & Active Navigation */
function initNavbarScroll() {
  const navbar = document.querySelector('.navbar');
  if (!navbar) return;
  window.addEventListener('scroll', () => {
    const isLight = document.body.classList.contains('light-mode');
    if (window.scrollY > 30) {
      navbar.style.background = isLight ? 'rgba(225, 232, 213, 0.96)' : 'rgba(12, 17, 9, 0.95)';
      navbar.style.boxShadow = isLight ? '0 10px 30px rgba(45, 56, 35, 0.12)' : '0 10px 30px rgba(0, 0, 0, 0.5)';
    } else {
      navbar.style.background = isLight ? 'rgba(225, 232, 213, 0.92)' : 'rgba(12, 17, 9, 0.85)';
      navbar.style.boxShadow = 'none';
    }
  });
}

/* Dark / Light Mode Switcher */
function initThemeToggle() {
  const themeBtn = document.getElementById('themeToggleBtn');
  if (!themeBtn) return;

  themeBtn.addEventListener('click', () => {
    document.body.classList.toggle('light-mode');
    const isLight = document.body.classList.contains('light-mode');
    themeBtn.innerHTML = isLight ? '🌙' : '☀️';
    themeBtn.setAttribute('title', isLight ? 'Switch to Dark Mode' : 'Switch to Light Mode');
  });
}

/* SHA-256 Copy Checksum */
function initCopyChecksum() {
  const copyBtn = document.getElementById('copyHashBtn');
  const hashText = document.getElementById('hashVal');

  if (copyBtn && hashText) {
    copyBtn.addEventListener('click', () => {
      navigator.clipboard.writeText(hashText.textContent.trim()).then(() => {
        const originalText = copyBtn.textContent;
        copyBtn.textContent = 'Copied! ✓';
        copyBtn.style.color = '#00dfa2';
        setTimeout(() => {
          copyBtn.textContent = originalText;
          copyBtn.style.color = '';
        }, 2000);
      });
    });
  }
}

/* Lightbox Modal for Screenshots */
function initScreenshotLightbox() {
  const shotCards = document.querySelectorAll('.shot-card');

  shotCards.forEach(card => {
    card.addEventListener('click', () => {
      const img = card.querySelector('img');
      if (!img) return;

      const overlay = document.createElement('div');
      overlay.style.cssText = `
        position: fixed;
        top: 0; left: 0; width: 100vw; height: 100vh;
        background: rgba(0, 0, 0, 0.85);
        backdrop-filter: blur(12px);
        z-index: 3000;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 24px;
        cursor: pointer;
      `;

      overlay.innerHTML = `
        <div style="position: relative; max-width: 90vw; max-height: 90vh;">
          <img src="${img.src}" alt="App Screenshot Enlarge" style="max-width: 100%; max-height: 85vh; border-radius: 16px; box-shadow: 0 20px 50px rgba(0,0,0,0.8); border: 1px solid rgba(0, 242, 254, 0.4);">
          <div style="position: absolute; top: -15px; right: -15px; width: 36px; height: 36px; border-radius: 50%; background: #00f2fe; color: #000; display: flex; align-items: center; justify-content: center; font-weight: bold;">✕</div>
        </div>
      `;

      document.body.appendChild(overlay);

      overlay.addEventListener('click', () => {
        overlay.remove();
      });
    });
  });
}
