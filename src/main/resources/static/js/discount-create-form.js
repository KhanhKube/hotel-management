document.addEventListener('DOMContentLoaded', function () {
    // Get elements
    const percentRadio = document.getElementById('percentRadio');
    const fixedRadio = document.getElementById('fixedRadio');
    const percentOption = document.getElementById('percentOption');
    const fixedOption = document.getElementById('fixedOption');
    const percentValue = document.getElementById('percentValue');
    const fixedValue = document.getElementById('fixedValue');
    const finalDiscountType = document.getElementById('finalDiscountType');
    const finalValue = document.getElementById('finalValue');

    // Update UI and enable/disable inputs
    function updateDiscountType() {
        if (percentRadio.checked) {
            // Enable percent option
            percentOption.classList.add('selected');
            fixedOption.classList.remove('selected');
            fixedOption.classList.add('disabled');
            
            percentValue.disabled = false;
            percentValue.required = true;
            fixedValue.disabled = true;
            fixedValue.required = false;
            fixedValue.value = ''; // Clear disabled input
            
            // Update hidden fields
            finalDiscountType.value = 'PERCENT';
        } else if (fixedRadio.checked) {
            // Enable fixed option
            fixedOption.classList.add('selected');
            percentOption.classList.remove('selected');
            percentOption.classList.add('disabled');
            
            fixedValue.disabled = false;
            fixedValue.required = true;
            percentValue.disabled = true;
            percentValue.required = false;
            percentValue.value = ''; // Clear disabled input
            
            // Update hidden fields
            finalDiscountType.value = 'PRICE';
        }
    }

    // Event listeners for radio buttons
    percentRadio.addEventListener('change', updateDiscountType);
    fixedRadio.addEventListener('change', updateDiscountType);

    // Click on entire option container to select
    percentOption.addEventListener('click', function (e) {
        if (e.target.type !== 'number') { // Don't trigger when clicking input
            percentRadio.checked = true;
            updateDiscountType();
        }
    });

    fixedOption.addEventListener('click', function (e) {
        if (e.target.type !== 'number') { // Don't trigger when clicking input
            fixedRadio.checked = true;
            updateDiscountType();
        }
    });

    // Update final value when inputs change
    percentValue.addEventListener('input', function() {
        if (percentRadio.checked) {
            finalValue.value = this.value;
        }
    });

    fixedValue.addEventListener('input', function() {
        if (fixedRadio.checked) {
            finalValue.value = this.value;
        }
    });

    // Initialize on page load
    updateDiscountType();

    // Form validation before submit
    document.querySelector('form').addEventListener('submit', function (e) {
        if (percentRadio.checked && !percentValue.value) {
            e.preventDefault();
            alert('Please enter a percentage value');
            percentValue.focus();
            return false;
        }
        
        if (fixedRadio.checked && !fixedValue.value) {
            e.preventDefault();
            alert('Please enter a fixed amount value');
            fixedValue.focus();
            return false;
        }

        // Update final value before submit
        if (percentRadio.checked) {
            finalValue.value = percentValue.value;
        } else if (fixedRadio.checked) {
            finalValue.value = fixedValue.value;
        }
    });
});
