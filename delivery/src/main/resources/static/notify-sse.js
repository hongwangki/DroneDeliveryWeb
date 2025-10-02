// notify-sse.js  — 모든 페이지 공통 알림 (모달 DOM/스타일 자동 주입 + SSE 구독)
(function(){
    // 1) 스타일/모달 DOM이 없으면 주입
    function ensureUi(){
        if(!document.getElementById('site-toast-style')){
            const style = document.createElement('style');
            style.id = 'site-toast-style';
            style.textContent = `
        .toast-backdrop{position:fixed; inset:0; display:none; align-items:center; justify-content:center; z-index:9999; background:rgba(0,0,0,.35)}
        .toast-card{width:min(480px,92vw); background:#fff; border-radius:16px; box-shadow:0 20px 60px rgba(0,0,0,.3); border:1px solid #e5e7eb; overflow:hidden}
        .toast-card .hd{padding:14px 16px; background:#111827; color:#fff; font-weight:700}
        .toast-card .bd{padding:18px 16px; color:#111827}
        .toast-card .ft{padding:12px 16px; display:flex; justify-content:flex-end; gap:10px; background:#f9fafb; border-top:1px solid #eef2f7}
        .btn{padding:8px 12px; border-radius:10px; border:1px solid #e5e7eb; background:#fff; cursor:pointer; font-weight:600}
        .btn.primary{background:#2563eb; color:#fff; border-color:#2563eb}
      `;
            document.head.appendChild(style);
        }
        if(!document.getElementById('site-toast')){
            const div = document.createElement('div');
            div.id = 'site-toast';
            div.className = 'toast-backdrop';
            div.setAttribute('aria-hidden','true');
            div.innerHTML = `
        <div class="toast-card" role="dialog" aria-modal="true" aria-labelledby="toast-title">
          <div class="hd" id="toast-title">배달 완료</div>
          <div class="bd">
            <p style="margin:0 0 6px"><strong id="toast-msg">배달이 완료되었습니다!</strong></p>
            <p style="margin:0; color:#6b7280">리뷰를 작성하시겠어요?</p>
          </div>
          <div class="ft">
            <button type="button" class="btn" id="toast-later">나중에</button>
            <button type="button" class="btn primary" id="toast-ok">확인</button>
          </div>
        </div>`;
            document.body.appendChild(div);
        }
    }

    function reviewUrl(orderId){ return `/reviews/new?orderId=${encodeURIComponent(orderId)}`; }

    function showDeliveredToast(orderId, msg){
        const backdrop = document.getElementById('site-toast');
        const msgEl = document.getElementById('toast-msg');
        const btnLater = document.getElementById('toast-later');
        const btnOK = document.getElementById('toast-ok');

        if(!backdrop) return;
        msgEl.textContent = msg || '배달이 완료되었습니다!';
        backdrop.style.display = 'flex';
        backdrop.classList.add('show');
        backdrop.setAttribute('aria-hidden','false');

        btnLater.onclick = () => {
            backdrop.classList.remove('show');
            backdrop.style.display = 'none';
            backdrop.setAttribute('aria-hidden','true');
        };
        btnOK.onclick = async () => {
            try { await fetch(`/orders/deliver/${orderId}`, { method:'POST' }); } catch(e) {}
            location.href = reviewUrl(orderId);
        };
    }

    function openSSE(){
        try{
            const es = new EventSource('/sse/stream', { withCredentials: true });
            es.addEventListener('ping', () => {});
            es.addEventListener('notify', (evt) => {
                try{
                    const data = JSON.parse(evt.data);
                    if (data.type === 'ORDER_DELIVERED' && data.orderId) {
                        // 현재 페이지가 이미 해당 주문 리뷰면 무시
                        const u = new URL(location.href);
                        if (u.pathname === '/reviews/new' && u.searchParams.get('orderId') == String(data.orderId)) return;
                        showDeliveredToast(data.orderId, data.message);
                    }
                }catch(e){ console.warn(e); }
            });
            es.onerror = () => { /* 브라우저가 자동 재연결 */ };
        }catch(e){
            console.warn('SSE not supported?', e);
        }
    }

    function init(){
        ensureUi();
        openSSE();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
