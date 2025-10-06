document.addEventListener('DOMContentLoaded', function () {
    // Status Toggle Functionality
    const statusToggle = document.getElementById('statusToggle');

    // Discount Type Selection
    const percentRadio = document.querySelector('input[value="percent"]');
    const priceRadio = document.querySelector('input[value="price"]');
    const percentOption = document.getElementById('percentOption');
    const priceOption = document.getElementById('priceOption');

    function updateDiscountType() {
        if (percentRadio.checked) {
            percentOption.classList.add('selected');
            priceOption.classList.remove('selected');
        } else if (priceRadio.checked) {
            priceOption.classList.add('selected');
            percentOption.classList.remove('selected');
        }
    }

    percentRadio.addEventListener('change', updateDiscountType);
    priceRadio.addEventListener('change', updateDiscountType);

    // Click on option container to select radio
    percentOption.addEventListener('click', function () {
        percentRadio.checked = true;
        updateDiscountType();
    });

    priceOption.addEventListener('click', function () {
        priceRadio.checked = true;
        updateDiscountType();
    });

    // Form submission
    document.querySelector('form').addEventListener('submit', function (e) {
        e.preventDefault();

        // Collect form data
        const formData = {
            voucherCode: document.getElementById('voucherCode').value,
            title: document.getElementById('title').value,
            startDate: document.getElementById('startDate').value,
            endDate: document.getElementById('endDate').value,
            totalUsageLimit: document.getElementById('totalUsageLimit').value,
            minimumGuests: document.getElementById('minimumGuests').value,
            status: statusToggle.checked ? 'ACTIVE' : 'INACTIVE',
            discountType: document.querySelector('input[name="discountType"]:checked').value,
            discountValue: percentRadio.checked ?
                document.getElementById('percent').value :
                document.getElementById('price').value,
            percentLimit: percentRadio.checked ? document.getElementById('percentLimit').value : null,
            roomTypes: Array.from(document.querySelectorAll('input[name="roomTypes"]:checked'))
                .map(cb => cb.value)
        };

        console.log('Form Data:', formData);
        alert('Form submitted! Check console for data.');
    });
});
