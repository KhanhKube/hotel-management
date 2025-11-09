// Cart Badge Functions - Backup implementation
// This file provides a fallback if header.html script doesn't load
(function () {
    console.log('cart-preview.js loaded');

    // Only define if not already defined
    if (typeof window.loadCartBadge === 'undefined') {
        console.log('Defining loadCartBadge from cart-preview.js');

        window.loadCartBadge = function () {
            console.log('=== Loading cart badge (cart-preview.js) ===');

            const badge = document.getElementById('cart-badge');
            if (!badge) {
                console.log('Badge element not found - user not logged in');
                return;
            }

            console.log('Fetching cart count from /cart/items...');

            fetch('/cart/items')
                .then(res => {
                    console.log('Cart API response status:', res.status);

                    if (res.status === 401 || res.status === 403) {
                        return [];
                    }

                    return res.json();
                })
                .then(data => {
                    if (!data) return;

                    console.log('Cart items count:', data.length);

                    if (!data || data.length === 0) {
                        badge.style.setProperty('display', 'inline-block', 'important');
                        badge.style.setProperty('background', '#999', 'important');
                        badge.textContent = '0';
                        console.log('Badge set to 0');
                    } else {
                        badge.style.setProperty('display', 'inline-block', 'important');
                        badge.style.setProperty('background', '#ff4444', 'important');
                        badge.textContent = data.length;
                        console.log('Badge set to', data.length);
                    }
                })
                .catch(err => {
                    console.error('Error loading cart badge:', err);
                });
        };

        window.loadCartPreview = window.loadCartBadge; // Alias

        // Auto-load on DOM ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function () {
                console.log('DOMContentLoaded (cart-preview.js), loading badge...');
                setTimeout(window.loadCartBadge, 200);
                setTimeout(window.loadCartBadge, 600);
            });
        } else {
            console.log('DOM already loaded (cart-preview.js), loading badge...');
            setTimeout(window.loadCartBadge, 200);
            setTimeout(window.loadCartBadge, 600);
        }
    } else {
        console.log('loadCartBadge already defined, skipping cart-preview.js definition');
    }
})();
