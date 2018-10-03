
var mymap = L.map('mapid').setView([48.86, 2.34], 13.4).on('change', function () {
    mymap.eachLayer(function (element) {
        element.fire('change');
    });
}).on('zoomend',function(){
}).on('moveend',function(){
}).on('towns', function () {
    mymap.eachLayer(function (element) {
        element.fire('towns');
    });
}).on('routes', function () {
    mymap.eachLayer(function (element) {
        element.fire('routes');
    });
}).on('next', function () {
    mymap.eachLayer(function (element) {
        element.fire('next');
    });
}).on('history', function () {
    mymap.eachLayer(function (element) {
        element.fire('history');
    });
});
mymap.doubleClickZoom.disable(); 
var baseMap = L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
    maxZoom: 17,
    id: 'mapbox.streets',
    accessToken: 'pk.eyJ1Ijoib3Vzc2FtYWF5b3ViIiwiYSI6ImNqZnJ6dDEwNTJrYXAyd21rNmYxZ2J1bGoifQ.u25oUw_d_MwuX1dsVGCvvw'
}).addTo(mymap);



function rescale (num, in_min, in_max, out_min, out_max) {
    return (num - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

function clear_layer(e){
    mymap.removeLayer(e.target)

}


var command = L.control({position: 'topleft'});
command.onAdd = function (map) {
    var div = L.DomUtil.create('div', 'test1');
    L.DomEvent.disableClickPropagation(div);
    div.innerHTML = '<div class="allbuttonbutton"><input type="checkbox" id="roadsection" name="feature" value="RoadSection" onchange="AddRoadSection(this)" unchecked /><label for="RoadSection">RoadSection</label></br><input type="checkbox" id="towns" name="feature" value="Towns" onchange="Towns(this)" unchecked /><label for="Towns">Towns</label></div><input type="checkbox" id="scales" name="feature" value="Towns" onchange="Routes(this)" unchecked /><label for="Routes">Routes</label></div></br><input type="checkbox" id="NextPassages" name="feature" value="NextPassages" onchange="NextPassages(this)" unchecked /><label for="NextPassages">NextPassages</label></div></br><input type="checkbox" id="History" name="feature" value="History" onchange="History(this)" unchecked /><label for="History">History</label></div>'; 
    return div;
};
command.addTo(mymap);


function History(element)
{
    if(element.checked){
        var command = L.control({position: 'topright'});
        command.onAdd = function (map) {
            var div = L.DomUtil.create('div', 'test');
            L.DomEvent.disableClickPropagation(div);
            div.innerHTML ='<p id="Date">Date: <input type="text" id="datepicker"></p>'; 
            return div;
        };
        command.addTo(mymap);
        history();
    }
    else{
        $("#Date").remove();
        mymap.fire("history"); 
    }
}
function history() {
    var scalespeed = d3.scaleLinear().domain([0,0.5,1]).range(["green","yellow", "red"]);
    $( "#datepicker" ).datepicker({
        onSelect: function(dateText) {
            var date = $(this).datepicker('getDate'),
                day  = date.getDate(),  
                month = date.getMonth() + 1,              
                year =  date.getFullYear();
            var xhttp = new XMLHttpRequest();
            xhttp.onreadystatechange = function() {
                if (this.readyState == 4 && this.status == 200) {
                    var result=JSON.parse(this.responseText)

                    result.forEach(function(element){
                        var geojsonMarkerOptions = {
                            radius: 8,
                            fillColor: scalespeed(element.delays/(element.delays+element.ontime+element.cancel)),
                            color: "#000",
                            weight: 1,
                            opacity: 1,
                            fillOpacity: 0.8
                        };
                        var feature = {};
                        feature['type'] = 'Feature';
                        feature['geometry'] ={"type": "Point","coordinates": [element.lon, element.lat]};
                        feature['properties'] =  {'name': element.name,'id': element.id,'cancel':element.cancel,'ontime':element.ontime,'delays':element.delays}
                        var m=L.geoJSON(feature, {
                            pointToLayer: function (feature, latlng) {
                                return L.circleMarker(latlng, geojsonMarkerOptions);
                            }

                        });
                        m.bindPopup('<h2>'+JSON.parse(feature.properties.name)+'</h2><h3>ID: '+feature.properties.id+'</h3><h3>Ontime: '+feature.properties.ontime+'</h3><h3>Delays: '+feature.properties.delays+'</h3><h3>Cancel: '+feature.properties.cancel+'</h3>').on('history',clear_layer);
                        mymap.addLayer(m);

                    });
                }
            };
            xhttp.open("GET", "http://localhost:8080/Stop/Day_History?year="+year+"&month="+month+"&day="+day+"", true);
            xhttp.send();
            mymap.fire('history');
        }
    });
}

function NextPassages(element)
{
    if(element.checked){
        nextPassages();
    }
    else{
        mymap.fire("next"); 
    }
}
function nextPassages(){
    var test=['ON_TIME','DELAYED','CANCELLED','NO_REPORT','','EARLY'];
    var scale = d3.scaleLinear().domain([0,1,2,3,4]).range(["green","yellow", "red","black","white","blue"]);
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var result=JSON.parse(this.responseText)
            result.forEach(function(element){
                var geojsonMarkerOptions;
                if(element.approach.arrivalStatus.length==2 && element.approach.expectedDepartureTime.length!=0){
                    geojsonMarkerOptions = {
                        radius: 8,
                        fillColor: "orange",
                        color: "#000",
                        weight: 1,
                        opacity: 1,
                        fillOpacity: 0.8
                    };
                }else{
                    geojsonMarkerOptions = {
                        radius: 8,
                        fillColor: scale(test.indexOf(element.approach.arrivalStatus.substring(1,element.approach.arrivalStatus.length-1))),
                        color: "#000",
                        weight: 1,
                        opacity: 1,
                        fillOpacity: 0.8
                    };
                }

                var feature = {};
                feature['type'] = 'Feature';
                feature['geometry'] ={"type": "Point","coordinates": [element.lon, element.lat]};
                feature['properties'] =  {'name': element.name,'id': element.id,'vehicleRef':element.approach.vehicleRef,'aimedArrivalTime':element.approach.aimedArrivalTime,'expectedDepartureTime':element.approach.expectedDepartureTime,'aimedDepartureTime':element.approach.aimedDepartureTime,'expectedArrivalTime':element.approach.expectedArrivalTime,'arrivalStatus':element.approach.arrivalStatus}
                var m=L.geoJSON(feature, {
                    pointToLayer: function (feature, latlng) {
                        return L.circleMarker(latlng, geojsonMarkerOptions);
                    }
                });
                m.bindPopup('<h2>'+JSON.parse(feature.properties.name)+'</h2><h3>ID: '+feature.properties.id+'</h3><h3>vehicleRef: '+feature.properties.vehicleRef+'</h3><h3>aimedDepartureTime: '+feature.properties.aimedDepartureTime+'</h3><h3>aimedArrivalTime: '+feature.properties.aimedArrivalTime+'</h3><h3>expectedArrivalTime: '+feature.properties.expectedArrivalTime+'</h3><h3>expectedDepartureTime: '+feature.properties.expectedDepartureTime+'</h3><h3>ArrivalStatus: '+feature.properties.arrivalStatus+'</h3>').on('next',clear_layer);
                mymap.addLayer(m);

            });
        }
    };
    xhttp.open("GET", "http://localhost:8080/Stop/NextPassages", true);
    xhttp.send();
}



function getRandomColor() {
    var letters = '0123456789ABCDEF';
    var color = '#';
    for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}
function Routes(element)
{
    if(element.checked){
        routes();
    }
    else{
        mymap.fire("routes"); 
    }
}
function routes(){
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var result=JSON.parse(this.responseText)
            result.forEach(function(element){   
                var feature = {};
                feature['type'] = 'Feature';
                var statesData = JSON.parse(JSON.parse(element.geoshape));
                feature['geometry'] =statesData;
                feature['properties'] =  {'long_name': element.long_name,'id':element.id};
                L.geoJSON(feature, {
                    style: {
                        weight: 4,
                        opacity: 0.6,
                        color: getRandomColor()
                    }

                }).addTo(mymap).bindPopup('<h2>'+JSON.parse(feature.properties.long_name)+'</h2>').on('routes',clear_layer).bringToBack();
            });
        }
    };
    xhttp.open("GET", "http://localhost:8080/Stop/Route", true);
    xhttp.send();
}


var popup = L.popup();
function onMapClick(e) {
    popup
        .setLatLng(e.latlng)
        .setContent('<h2>'+e.layer.feature.properties.name.toString() +'</h2><h3>Population: '+e.layer.feature.properties.total_pop+'</h3><h3>Density: '+e.layer.feature.properties.density+'</h3>')
        .openOn(mymap);
}

function Towns(element)
{
    if(element.checked){
        towns();
    }
    else{
        mymap.fire("towns"); 
    }
}
var scalepop = d3.scaleLinear().domain([ 0, 5000, 25000, 46000]).range([ "#99ccff", "#6699ff", "#0000ff", "#000099"]);
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
                var area=parseFloat(element.shape_area);
                 var density=element.total_pop/(area/1000000);
                feature['properties'] =  {'name': element.name,'area': element.shape_area,'total_pop': element.total_pop,'density':density};
               
                L.geoJSON(feature, {
                    style: {
                        weight: 2,
                        opacity: 0.5,
                        color: 'white',
                        dashArray: '3',
                        fillOpacity: 0.5,
                        fillColor: (function() {
                            if(feature.properties.total_pop!=0){
                                //console.log(feature.properties.total_pop/(feature.properties.area/1000))
                                return scalepop(feature.properties.total_pop/(feature.properties.area/1000000)); 
                            }
                            else{
                                return '#808080';
                            }

                        })()
                    }
                }).addTo(mymap).on('dblclick', onMapClick).bringToBack().on('towns',clear_layer);
            });
        }
    };
    xhttp.open("GET", "http://localhost:8080/Town/", true);
    xhttp.send();
}




function AddRoadSection(element)
{
    if(element.checked){
        var command = L.control({position: 'topright'});
        var currentcat=0;
        var currenthour=0;
        command.onAdd = function (map) {
            boole=true;
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
        catday(0,0);
    }
    else{
        document.body.removeChild(document.body.getElementsByTagName('style')[0]);
        document.body.removeChild(document.body.getElementsByTagName('style1')[0]);
        $('.test').remove();
        mymap.fire('change');
    }
}
var scalespeed = d3.scaleLinear().domain([0.0,20.0,70.0]).range(["red","yellow", "green"]);
var boole=false;
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