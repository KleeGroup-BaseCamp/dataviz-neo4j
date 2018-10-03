var mymap = L.map('mapid').setView([48.845, 2.34], 13.4).on('change', function () {
    mymap.eachLayer(function (element) {
        element.fire('change');
    });
}).on('zoomend',function(){
}).on('moveend',function(){
});;

var baseMap = L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}', {
    attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://mapbox.com">Mapbox</a>',
    maxZoom: 17,
    id: 'mapbox.streets',
    accessToken: 'pk.eyJ1Ijoib3Vzc2FtYWF5b3ViIiwiYSI6ImNqZnJ6dDEwNTJrYXAyd21rNmYxZ2J1bGoifQ.u25oUw_d_MwuX1dsVGCvvw'
}).addTo(mymap);

function getRandomColor() {
  var letters = '0123456789ABCDEF';
  var color = '#';
  for (var i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)];
  }
  return color;
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
                        color: getRandomColor(),
                        }
                    
                }).addTo(mymap).bindPopup('<h2>'+JSON.parse(feature.properties.long_name)+'</h2>');
            });
        }
    };
    xhttp.open("GET", "http://localhost:8080/Stop/Route", true);
    xhttp.send();
}
towns();