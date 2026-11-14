/* ==========================================================================
   Knapsack E-Commerce Client Scripts
   ========================================================================== */

document.addEventListener('DOMContentLoaded', () => {
    // Quantity Buttons Handlers
    document.querySelectorAll('.qty-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const container = e.target.closest('.qty-control');
            if (!container) return;
            const input = container.querySelector('.qty-input');
            if (!input) return;

            let currentVal = parseInt(input.value) || 1;
            if (e.target.classList.contains('qty-plus')) {
                input.value = currentVal + 1;
            } else if (e.target.classList.contains('qty-minus')) {
                if (currentVal > 1) {
                    input.value = currentVal - 1;
                }
            }

            // If inside a form auto-submit on change if class present
            const form = container.closest('form.auto-submit');
            if (form) {
                form.submit();
            }
        });
    });

    // Image Gallery Thumbnail Switcher
    const mainImg = document.getElementById('mainProductImage');
    document.querySelectorAll('.thumb-item').forEach(thumb => {
        thumb.addEventListener('click', () => {
            if (mainImg) {
                mainImg.src = thumb.src;
                document.querySelectorAll('.thumb-item').forEach(t => t.classList.remove('active'));
                thumb.classList.add('active');
            }
        });
    });
});
