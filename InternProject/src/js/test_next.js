
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

                }).addTo(mymap).bindPopup('<h2>'+JSON.parse(feature.properties.long_name)+'</h2>');
            });
        }
    };
    xhttp.open("GET", "http://localhost:8080/Stop/Route", true);
    xhttp.send();
}
towns();
var test=['ON_TIME','DELAYED','CANCELLED','NO_REPORT','','EARLY'];
var scale = d3.scaleLinear().domain([0,1,2,3,4]).range(["green","yellow", "red","black","white","blue"]);
var xhttp = new XMLHttpRequest();
xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
        var result=JSON.parse(this.responseText)
        
        result.forEach(function(element){
            var geojsonMarkerOptions = {
                radius: 8,
                fillColor: scale(test.indexOf(element.approach.arrivalStatus.substring(1,element.approach.arrivalStatus.length-1))),
                color: "#000",
                weight: 1,
                opacity: 1,
                fillOpacity: 0.8
            };
            var feature = {};
            feature['type'] = 'Feature';
            feature['geometry'] ={"type": "Point","coordinates": [element.lon, element.lat]};
            feature['properties'] =  {'name': element.name,'id': element.id,'vehicleRef':element.approach.vehicleRef,'aimedArrivalTime':element.approach.aimedArrivalTime,'aimedDepartureTime':element.approach.aimedDepartureTime,'expectedArrivalTime':element.approach.expectedArrivalTime,'arrivalStatus':element.approach.arrivalStatus}
            var m=L.geoJSON(feature, {
                pointToLayer: function (feature, latlng) {
                    return L.circleMarker(latlng, geojsonMarkerOptions);
                }
            });
            m.bindPopup('<h2>'+JSON.parse(feature.properties.name)+'</h2><h3>ID: '+feature.properties.id+'</h3><h3>vehicleRef: '+feature.properties.vehicleRef+'</h3><h3>aimedDepartureTime: '+feature.properties.aimedDepartureTime+'</h3><h3>aimedArrivalTime: '+feature.properties.aimedArrivalTime+'</h3><h3>expectedArrivalTime: '+feature.properties.expectedArrivalTime+'</h3><h3>ArrivalStatus: '+feature.properties.arrivalStatus+'</h3>');
            mymap.addLayer(m);

        });
    }
};
xhttp.open("GET", "http://localhost:8080/Stop/NextPassages", true);
xhttp.send();



/*markers._getExpandedVisibleBounds = function () {
    return markers._map.getBounds();
};*/ 
//mymap.addLayer(markers);

