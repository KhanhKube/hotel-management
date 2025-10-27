/**
 * AI Chat Widget JavaScript
 * Handles chat functionality and API communication
 */

let isChatOpen = false;

/**
 * Toggle chat widget visibility
 */
function toggleAIChat() {
    const widget = document.getElementById('aiChatWidget');
    isChatOpen = !isChatOpen;
    widget.style.display = isChatOpen ? 'flex' : 'none';
    
    if (isChatOpen) {
        document.getElementById('chatInput').focus();
    }
}

/**
 * Add message to chat area
 * @param {string} text - Message text (can contain HTML)
 * @param {boolean} isUser - True if message from user, false if from bot
 */
function addMessage(text, isUser) {
    const chatArea = document.getElementById('chatArea');
    const welcome = document.getElementById('welcomeMsg');
    if (welcome) welcome.remove();
    
    const msg = document.createElement('div');
    msg.className = 'chat-message ' + (isUser ? 'user' : 'bot');
    msg.innerHTML = '<div class="message-bubble">' + text + '</div>';
    chatArea.appendChild(msg);
    chatArea.scrollTop = chatArea.scrollHeight;
}

/**
 * Send message from input
 */
function sendMessage() {
    const input = document.getElementById('chatInput');
    const text = input.value.trim();
    if (!text) return;
    
    addMessage(text, true);
    input.value = '';
    
    setTimeout(() => processMessage(text), 500);
}

/**
 * Send example message (from chip click)
 * @param {string} text - Example message text
 */
function sendExampleMessage(text) {
    document.getElementById('chatInput').value = text;
    sendMessage();
}

/**
 * Handle Enter key press in input
 * @param {Event} event - Keyboard event
 */
function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

/**
 * Process user message and call API
 * @param {string} text - User message text
 */
function processMessage(text) {
    fetch('/hotel-management/room/search', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ query: text })
    })
        .then(res => res.json())
        .then(data => {
            const roomIds = data.data || [];
            
            if (roomIds.length === 0) {
                addMessage('Xin lỗi, không tìm thấy phòng phù hợp với yêu cầu "' + text + '". Bạn có thể thử tìm kiếm khác!', false);
                return;
            }
            
            addMessage('Tôi đã tìm thấy ' + roomIds.length + ' phòng phù hợp với yêu cầu của bạn:', false);
            
            roomIds.forEach((roomId, i) => {
                setTimeout(() => {
                    const link = '/hotel/room/' + roomId;
                    const html = '<strong>Phòng #' + roomId + '</strong><br>' +
                                '<a href="' + link + '" target="_blank">👉 Xem chi tiết và đặt phòng</a>';
                    addMessage(html, false);
                }, i * 300);
            });
        })
        .catch(err => {
            console.error('Error:', err);
            addMessage('Xin lỗi, có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại!', false);
        });
}

/**
 * Close chat with ESC key
 */
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape' && isChatOpen) {
        toggleAIChat();
    }
});
