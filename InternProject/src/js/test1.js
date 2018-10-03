
var mymap = L.map('mapid').setView([48.86, 2.34], 11).on('change', function () {
    mymap.eachLayer(function (element) {
        element.fire('change');
    })});

L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
    maxZoom: 17,
    id: 'mapbox.streets',
    accessToken: 'pk.eyJ1Ijoib3Vzc2FtYWF5b3ViIiwiYSI6ImNqZnJ6dDEwNTJrYXAyd21rNmYxZ2J1bGoifQ.u25oUw_d_MwuX1dsVGCvvw'
}).addTo(mymap);

function onClick(e){
    if(e.layer.feature.properties.speed!=null){
        console.log(e.layer.feature.properties.name+" : "+e.layer.feature.properties.speed);
    }
    else{
        console.log("NO DATA")
    }

}


var command = L.control({position: 'topright'});
command.onAdd = function (map) {
    var div = L.DomUtil.create('div', 'test');
    L.DomEvent.disableClickPropagation(div);
    div.innerHTML ='<p>Date: <input type="text" id="datepicker"></p>'; 
    return div;
};

command.addTo(mymap);

function rescale (num, in_min, in_max, out_min, out_max) {
    return (num - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}
var markers = L.markerClusterGroup({
    chunkedLoading: true,
    disableClusteringAtZoom: 15,
    spiderfyOnMaxZoom: false
});
function getRandomColor() {
    var letters = '0123456789ABCDEF';
    var color = '#';
    for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}
function clear_layer(e){
    mymap.removeLayer(e.target)

}

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
                feature['properties'] =  {'long_name': element.long_name,'id':element.id};
                L.geoJSON(feature, {
                    style: {
                        weight: 4,
                        opacity: 0.6,
                        color: getRandomColor()
                    }

                }).addTo(mymap).bindPopup('<h2>'+JSON.parse(feature.properties.long_name)+'</h2>');
            });
        }
    };
    xhttp.open("GET", "http://localhost:8080/Stop/Route", true);
    xhttp.send();
}
towns();

var scalespeed = d3.scaleLinear().domain([0,0.5,1]).range(["green","yellow", "red"]);
$( function() {
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
                        m.bindPopup('<h2>'+JSON.parse(feature.properties.name)+'</h2><h3>ID: '+feature.properties.id+'</h3><h3>Ontime: '+feature.properties.ontime+'</h3><h3>Delays: '+feature.properties.delays+'</h3><h3>Cancel: '+feature.properties.cancel+'</h3>').on('change',clear_layer);
                        mymap.addLayer(m);

                    });
                }
            };
            xhttp.open("GET", "http://localhost:8080/Stop/Day_History?year="+year+"&month="+month+"&day="+day+"", true);
            xhttp.send();
            mymap.fire('change');
        }
    });
} );

/*markers._getExpandedVisibleBounds = function () {
    return markers._map.getBounds();
};*/ 
//mymap.addLayer(markers);

