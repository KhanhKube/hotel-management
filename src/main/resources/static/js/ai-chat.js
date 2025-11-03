/**
 * AI Chat Widget JavaScript
 * Handles chat functionality and API communication
 */

let isChatOpen = false;
const CHAT_HISTORY_KEY = 'aiChatHistory';

/**
 * Save chat history to localStorage
 */
function saveChatHistory() {
    const chatArea = document.getElementById('chatArea');
    const messages = chatArea.innerHTML;
    localStorage.setItem(CHAT_HISTORY_KEY, messages);
    console.log('Chat history saved');
}

/**
 * Load chat history from localStorage
 */
function loadChatHistory() {
    const savedHistory = localStorage.getItem(CHAT_HISTORY_KEY);
    if (savedHistory) {
        const chatArea = document.getElementById('chatArea');
        chatArea.innerHTML = savedHistory;
        chatArea.scrollTop = chatArea.scrollHeight;
        console.log('Chat history loaded');
    }
}

/**
 * Clear chat history
 */
function clearChatHistory() {
    localStorage.removeItem(CHAT_HISTORY_KEY);
    const chatArea = document.getElementById('chatArea');
    chatArea.innerHTML = `
        <div id="welcomeMsg" class="welcome-message">
            <div class="welcome-icon">👋</div>
            <div class="welcome-title">Xin chào!</div>
            <div class="welcome-text">Tôi là trợ lý AI của khách sạn. Hãy cho tôi biết bạn đang tìm loại phòng nào?</div>
            <div class="welcome-examples">
                <div class="example-chip" onclick="sendExampleMessage('Tôi muốn phòng VIP')">Phòng VIP</div>
                <div class="example-chip" onclick="sendExampleMessage('Tìm phòng giá rẻ')">Phòng giá rẻ</div>
                <div class="example-chip" onclick="sendExampleMessage('Phòng Deluxe')">Phòng Deluxe</div>
            </div>
        </div>
    `;
    console.log('Chat history cleared');
}

/**
 * Toggle chat widget visibility
 */
function toggleAIChat() {
    const widget = document.getElementById('aiChatWidget');
    isChatOpen = !isChatOpen;
    widget.style.display = isChatOpen ? 'flex' : 'none';

    if (isChatOpen) {
        // Load chat history when opening
        loadChatHistory();
        document.getElementById('chatInput').focus();
    }
}

/**
 * Add message to chat area
 * @param {string} text - Message text (can contain HTML)
 * @param {boolean} isUser - True if message from user, false if from bot
 * @param {boolean} saveHistory - Whether to save to localStorage (default: true)
 */
function addMessage(text, isUser, saveHistory = true) {
    const chatArea = document.getElementById('chatArea');
    const welcome = document.getElementById('welcomeMsg');
    if (welcome) welcome.remove();

    const msg = document.createElement('div');
    msg.className = 'chat-message ' + (isUser ? 'user' : 'bot');
    msg.innerHTML = '<div class="message-bubble">' + text + '</div>';
    chatArea.appendChild(msg);
    chatArea.scrollTop = chatArea.scrollHeight;

    // Save chat history after adding message
    if (saveHistory) {
        saveChatHistory();
    }
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
 * Get room details by ID
 * @param {number} roomId - Room ID
 * @returns {Promise} Room details
 */
async function getRoomDetails(roomId) {
    try {
        const response = await fetch('/hotel-management/room/api/list');
        const data = await response.json();
        const rooms = data.data || data || [];

        // Find room by ID
        const room = rooms.find(r => r.roomId === roomId);
        return room;
    } catch (err) {
        console.error('Error fetching room details:', err);
        return null;
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
        body: JSON.stringify({query: text})
    })
        .then(res => res.json())
        .then(async data => {
            const roomIds = data.data || [];

            if (roomIds.length === 0) {
                addMessage('Xin lỗi, không tìm thấy phòng phù hợp với yêu cầu "' + text + '". Bạn có thể thử tìm kiếm khác!', false);
                return;
            }

            addMessage('🎉 Tôi đã tìm thấy ' + roomIds.length + ' phòng phù hợp với yêu cầu của bạn:', false);

            // Get room details for each room ID
            for (let i = 0; i < roomIds.length; i++) {
                const roomId = roomIds[i];

                setTimeout(async () => {
                    const room = await getRoomDetails(roomId);
                    const link = '/hotel/room/' + roomId;

                    if (room) {
                        const priceFormatted = room.price ? room.price.toLocaleString('vi-VN') : 'N/A';
                        const html = `
                            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); 
                                        padding: 15px; border-radius: 12px; color: white; margin: 5px 0;">
                                <div style="font-size: 1.1rem; font-weight: 600; margin-bottom: 8px;">
                                    <i class="fas fa-bed"></i> ${room.roomType || 'Phòng #' + roomId}
                                </div>
                                <div style="font-size: 0.9rem; opacity: 0.95; margin-bottom: 8px;">
                                    <i class="fas fa-door-open"></i> Phòng số: ${room.roomNumber || roomId}
                                    <br>
                                    <i class="fas fa-money-bill-wave"></i> Giá: <strong>${priceFormatted} VNĐ/đêm</strong>
                                </div>
                                <a href="${link}" 
                                   style="display: inline-block; background: white; color: #667eea; 
                                          padding: 8px 16px; border-radius: 8px; text-decoration: none; 
                                          font-weight: 600; transition: all 0.3s;"
                                   onmouseover="this.style.transform='scale(1.05)'" 
                                   onmouseout="this.style.transform='scale(1)'">
                                    <i class="fas fa-arrow-right"></i> Xem chi tiết & Đặt phòng
                                </a>
                            </div>
                        `;
                        addMessage(html, false);
                    } else {
                        // Fallback if room details not found
                        const html = `
                            <div style="background: #f8f9fa; padding: 12px; border-radius: 8px; 
                                        border-left: 4px solid #667eea;">
                                <strong>Phòng #${roomId}</strong><br>
                                <a href="${link}" style="color: #667eea; text-decoration: none; font-weight: 600;">
                                    👉 Xem chi tiết và đặt phòng
                                </a>
                            </div>
                        `;
                        addMessage(html, false);
                    }
                }, i * 400);
            }
        })
        .catch(err => {
            console.error('Error:', err);
            addMessage('Xin lỗi, có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại!', false);
        });
}

/**
 * Close chat with ESC key
 */
document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && isChatOpen) {
        toggleAIChat();
    }
});

/**
 * Initialize chat on page load
 */
document.addEventListener('DOMContentLoaded', function () {
    console.log('AI Chat initialized');
    // Don't auto-load history here, only when opening chat
    // This prevents issues with elements not being ready
});
