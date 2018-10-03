
var mymap = L.map('mapid').setView([48.86, 2.34], 13.4).on('change', function () {
    mymap.eachLayer(function (element) {
        element.fire('change');
    });
}).on('zoomend',function(){
}).on('moveend',function(){
});;
mymap.doubleClickZoom.disable(); 
var baseMap = L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
    maxZoom: 17,
    id: 'mapbox.streets',
    accessToken: 'pk.eyJ1Ijoib3Vzc2FtYWF5b3ViIiwiYSI6ImNqZnJ6dDEwNTJrYXAyd21rNmYxZ2J1bGoifQ.u25oUw_d_MwuX1dsVGCvvw'
}).addTo(mymap);

var command = L.control({position: 'topright'});
var currentcat=0;
var currenthour=0;
command.onAdd = function (map) {
    var div = L.DomUtil.create('div', 'test');
    L.DomEvent.disableClickPropagation(div);
    div.innerHTML = '<div class="range"><input type="range" min="1" max="5" steps="1" value="1"><ul class="range-labels"><li class="active selected">JOHV</li><li>JOVS</li><li>SAHV</li><li>SAVS</li><li>DIJFP</li></ul></div><div class="range1"><input type="range" min="1" max="24" steps="1" value="1"><ul class="range-hour"><li class="active selected">0</li><li>1</li><li>2</li><li>3</li><li>4</li><li>5</li><li>6</li><li>7</li><li>8</li><li>9</li><li>10</li><li>11</li><li>12</li><li>13</li><li>14</li><li>15</li><li>16</li><li>17</li><li>18</li><li>19</li><li>20</li><li>21</li><li>22</li><li>23</li></ul></div>'; 
    return div;
};

command.addTo(mymap);
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
    currentcat=curVal-1;
    catday(curVal-1,currenthour);
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
    currenthour=curVal-1;
    catday(currentcat,curVal-1);
    curLabel.addClass('active selected');
    return style;
};

$rangeInput1.on('input', function () {
    sheet1.textContent = getTrackStyle1(this);
});

// Change input value on label click
$('.range-hour li').on('click', function () {
    var index = $(this).index();

    $rangeInput1.val(index + 1).trigger('input');

});

function rescale (num, in_min, in_max, out_min, out_max) {
    return (num - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

function clear_layer(e){
    mymap.removeLayer(e.target)

}
 var popup = L.popup();
function onMapClick(e) {
    popup
        .setLatLng(e.latlng)
        .setContent('<h2>'+e.layer.feature.properties.name.toString() +'</h2><h3>Population: '+e.layer.feature.properties.total_pop+'</h3>')
        .openOn(mymap);
}
var scalepop = d3.scaleLinear().domain([ 0, 20000, 50000, 237636]).range([ "#99ccff", "#6699ff", "#0000ff", "#000099"]);
function towns(){
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var result=JSON.parse(this.responseText)
            result.forEach(function(element){   
                var feature = {};
                feature['type'] = 'Feature';
                var statesData = JSON.parse(JSON.parse(element.geoshape));
                feature['geometry'] =statesData;
                feature['properties'] =  {'name': element.name,'total_pop': element.total_pop};
               
                L.geoJSON(feature, {
                    style: {
                        weight: 2,
                        opacity: 0.5,
                        color: 'white',
                        dashArray: '3',
                        fillOpacity: 0.4,
                        fillColor: (function() {
                            if(feature.properties.total_pop!=0){
                                return scalepop(feature.properties.total_pop); 
                            }
                            else{
                                return '#808080';
                            }

                        })()
                    }
                }).addTo(mymap).on('dblclick', onMapClick).bringToBack();
            });
        }
    };
    xhttp.open("GET", "http://localhost:8080/Town/", true);
    xhttp.send();
    catday(0,0);
}



var scalespeed = d3.scaleLinear().domain([0.0,20.0,70.0]).range(["red","yellow", "green"]);

function catday(cat,hour){
    var type=["JOHV","JOVS","SAHV","SAVS","DIJFP"];
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            mymap.fire('change');
            var result=JSON.parse(this.responseText)
            result.forEach(function(element){   
                var feature = {};
                feature['type'] = 'Feature';
                var statesData = JSON.parse(JSON.parse(element.geoshape));
                feature['geometry'] =statesData;
                feature['properties'] =  {'id': element.id_arc_tra,'speed': element.hour.speed,'rateFlow': element.hour.rateFlow,'rate': element.hour.rate};
                L.geoJSON(feature, {
                    style: {
                        weight: rescale(feature.properties.rateFlow,0,6000,1,26),
                        color: (function() {
                            if(feature.properties.speed!=null && feature.properties.speed<=110 ){
                                return scalespeed(feature.properties.speed); 
                            }
                            else{
                                return '#ff00ff';
                            }
                        })()
                    }
                }).addTo(mymap).bindPopup('<h2>'+JSON.parse(feature.properties.id)+'</h2><h3>Rate of Flow: '+feature.properties.rateFlow+'</h3><h3>Speed: '+feature.properties.speed+'</h3>').on('change',clear_layer);
            });
        }
    };
    xhttp.open("GET", "http://localhost:8080/RoadSection/"+type[cat]+"/"+hour, true);
    xhttp.send();
}
towns();