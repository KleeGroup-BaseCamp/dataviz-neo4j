


//----------------------------------------------------------------------------------------------------

var sheet = document.createElement('style'),  
    $rangeInput = $('.range input'),
    prefs = ['webkit-slider-runnable-track', 'moz-range-track', 'ms-track'];
document.body.appendChild(sheet);

var getTrackStyle = function (el) {  
    var curVal = el.value,
        val = (curVal - 1) * 5,
        style = '';

    // Set active label
    $('.range-labels li').removeClass('active selected');

    var curLabel = $('.range-labels').find('li:nth-child(' + curVal + ')');

    curLabel.addClass('active selected');
    return style;
}

$rangeInput.on('input', function () {
    sheet.textContent = getTrackStyle(this);
});

// Change input value on label click
$('.range-labels li').on('click', function () {
    var index = $(this).index();

    $rangeInput.val(index + 1).trigger('input');

});





//----------------------------------------------------------------------------------------------------

var sheet1 = document.createElement('style1'),  
    $rangeInput1 = $('.range1 input'),
    prefs = ['webkit-slider-runnable-track', 'moz-range-track', 'ms-track'];

document.body.appendChild(sheet1);

var getTrackStyle1 = function (el) {
    var curVal = el.value,
        val = (curVal - 1) * 26,
        style = '';

    // Set active label
    $('.range-hour li').removeClass('active selected');

    var curLabel = $('.range-hour').find('li:nth-child(' + curVal + ')');
    
    curLabel.addClass('active selected');
    return style;
}

$rangeInput1.on('input', function () {
    sheet1.textContent = getTrackStyle1(this);
});

// Change input value on label click
$('.range-hour li').on('click', function () {
    var index = $(this).index();

    $rangeInput1.val(index + 1).trigger('input');

});


