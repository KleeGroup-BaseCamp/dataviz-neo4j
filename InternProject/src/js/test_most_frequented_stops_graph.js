var width = window.innerWidth,
    height = window.innerHeight;

// Lattitudes et longitudes du centre de Paris
var center_lat = 48.8534100,
    center_lon = 2.3488000;

var NB_ROUTES = 5;

// Couleurs pour les différentes lignes
var color = d3.scaleSequential()
            .domain([0, NB_ROUTES-1])
            .interpolator(d3.interpolateHslLong("#B40404", "#B40431"));

// Données initiales 
var catDays = [ {"name":"JOHV", "features":{}} , 
        {"name":"JOVS", "features":{}}, 
        {"name":"SAHV", "features":{}}, 
        {"name":"SAVS", "features":{}},
        {"name":"DIJFP", "features":{}}
       ];

// DOM

const svg = d3.select('body').append("svg")
    .attr("id", "svg")
    .attr("width", width)
    .attr("height", height);

var map = svg.append("g").attr("class", "map");
var projection = d3.geoConicConformal()//geo.albers()
    .center([center_lon, center_lat])
    .scale(500000)
    .translate([width / 2, height / 2+20]);

// Boutons

var svgContainer = svg.style('position', 'absolute')
                       .style('display', 'block');

var circles = svgContainer.selectAll(".buttons")
                           .data(catDays)
                           .enter();     

var circleAttributes = circles.append("circle")
                        .attr("id", function(d,i){
                            return "cercle_" + i ; 
                        })
                       .attr("cx", function(d,i) { return width -100- (5-i)*(40)})
                       .attr("cy", 50)
                       .attr("r", function (d) { return 15; })
                       .attr("fill-opacity", 0.6)
                       .attr("fill", 'black')
                       .attr("stroke-width", 1.5)
                       .attr("stroke", 'black')
                       .text(function(d){ return d})
                       .on("click", function(d, i){

                          // Affichage des boutons
                          for(var j = 0 ; j < 5 ; j++){
                            d3.select("#cercle_"+j).attr("fill",  'black').attr("stroke", 'black');
                            d3.select("#text_"+j).attr("fill",  'black');
                          }
                          d3.select(this).attr("fill", '#688CE4').attr("stroke", '#688CE4');
                          d3.select("#text_" + i ).attr("fill", '#688CE4'); 

                          // Mise à jour du graphe
                          updateGraph(catDays[i].features)

                       });

var circleText = svgContainer.selectAll("textButton")
                        .data(catDays)
                        .enter()
                        .append("text");

var textLabels = circleText.attr("x", function(d, i) {return width -113- (5-i)*80})
                 .attr("y", function(d) { return 30; })
                 .text( function (d) { return d.name; })
                 .attr("font-family", "sans-serif")
                 .attr("font-size", "12px")
                 .attr("fill", "black")
                 .attr("id", function(d,i){
                    return "text_" + i ; 
                   })
                 .attr("class", "textButton");

// Obtention des données
catDAys = getData(catDays,0);

function getData(d, i){

  if( i !== 5){
    var jqxhr = jQuery.get( "http://localhost:8080/StopsGraph/Graph/" + d[i].name, function(data) {
      d[i].features = createFeatures(data) ;
    })
    .done(function() {
      if (d[i].name === "JOHV"){
        alert( "Vous pouvez d" + "\351" + "sormais afficher " + d[i].name );
        updateGraph(d[i].features);
        d3.select("#cercle_0").attr("fill", '#688CE4').attr("stroke", '#688CE4');
        d3.select("#text_0" ).attr("fill", '#688CE4'); 
        return getData(d, i+1);
      }
      else{
        alert( "Vous pouvez d" + "\351" + "sormais afficher " + d[i].name );
        return getData(d, i+1);
      }

    })
    .fail(function() {
      alert( "error" );
    })
  }
  else{
    return d ; 
  }
}

function updateGraph(data) {

  // Vide la map
  map.selectAll(".link").remove();
  map.selectAll(".node").remove();
  map.selectAll(".label").remove();
  map.selectAll(".linklabel").remove(); 

  // Initialisation des couleurs
  colors = arrayColor(data.linksFeatures);

  var links = map .selectAll("link")
                  .data(data.linksFeatures)
                  .enter().append("line")
                  .attr("class", "link")
                  .attr('x1', function(d){
                    return projection(d.geometry.coordinates)[0]
                  })
                  .attr('y1', function(d){
                    return projection(d.geometry.coordinates)[1]
                  })
                  .attr('x2', function(d){
                    return projection([d.geometry.coordinates[2], d.geometry.coordinates[3]])[0]
                  })
                  .attr('y2', function(d){
                    return projection([d.geometry.coordinates[2], d.geometry.coordinates[3]])[1]
                  })
                  .attr("stroke", function(d){ 
                    return color(colors.indexOf(d.properties.name)); 
                  })
                  .attr("stroke-width", function(d){ 
                    return d.properties.nb_vald / (100000) + 5 ; 
                  })
                  .attr("id", function(d, i){ return ".line_" + i});

  var nodes = map.selectAll("node")
                 .data(data.nodesFeatures)
                 .enter().append("circle") 
                 .attr("class", "node")
                 .style("fill", '#666666')
                 .style("stroke-width", 1.5)
                 .style("stroke", 'black')
                 .attr('cx', function(d){
                    return projection(d.geometry.coordinates)[0]; })
                 .attr('cy', function(d){
                    return projection(d.geometry.coordinates)[1]; })
                 .attr('r', function(d){
                  return d.properties.nb_vald/22000; })

                 .on("mouseover", function(d, i){
                    var neighbourNodes = [];

                    d3.select(this).style("fill", "#688CE4").style("stroke", "#688CE4")
                    d3.selectAll("line").style("stroke-opacity", function(dl, j){
                      if (dl.properties.source_name.localeCompare(d.properties.name) === 0 ){
                        neighbourNodes.push(dl.properties.target_name);
                        return 0.4;
                      }
                      else if (dl.properties.target_name.localeCompare(d.properties.name) === 0){
                        neighbourNodes.push(dl.properties.source_name);
                        return 0.4;
                      }
                      else{
                        return 0.12;
                      }
                    })
                    d3.selectAll(".label").text( function(dl, j){
                      if (neighbourNodes.indexOf(dl.properties.name) >=0 || i === j ){
                        return dl.properties.name;
                      }
                      else{
                        return "";
                      }
                    }).style("fill", function(dl, j){ if (i===j) {return "#3E5CA1"}});
                    d3.selectAll(".linklabel").text( function(dl,){
                      if (dl.properties.source_name.localeCompare(d.properties.name) === 0  
                          || dl.properties.target_name.localeCompare(d.properties.name) === 0){                    
                        return dl.properties.name;
                      }
                      else{
                        return "";
                      }
                    })
                  })

                 .on("mouseout", function(d){
                    d3.select(this).style("fill", "#666666").style("stroke", "black")
                                //   .attr("fill-opacity", 0.6)
                                //   .attr("fill", '#666666')
                                //   .attr("stroke-width", 1.5)
                                //   .attr("stroke", 'black')
                    d3.selectAll("line").style("stroke-opacity",0.12);
                    d3.selectAll(".label").text( function(dl, j){return dl.properties.name; }).style("fill", "black");
                    d3.selectAll(".linklabel").text("");
                 })

  var nodeLabels =  map.selectAll("label")
                    .data(data.nodesFeatures)
                    .enter().append("text")
                    .attr("dx", 18) 
                    .attr("dy", ".35em") 
                    .attr("x", function(d){
                      return projection(d.geometry.coordinates)[0];
                    })
                    .attr("y", function(d){
                      return projection(d.geometry.coordinates)[1];
                    })
                    .text(function(d) {return d.properties.name})
                    .attr("class", "label");

  var linksLabels = map.selectAll(".linkLabel") 
        .data(data.linksFeatures)
        .enter().append("text")
        .attr("class","labelText")
        .attr("text-anchor", "start")
        .attr("x", function(d){
          return (projection(d.geometry.coordinates)[0] + projection([d.geometry.coordinates[2], d.geometry.coordinates[3]])[0])/2;
                    })
        .attr("y", function(d){
          return (projection(d.geometry.coordinates)[1]
                + projection([d.geometry.coordinates[2], d.geometry.coordinates[3]])[1])/2;
                    })
        .text(function(d) { return d.properties.name;}) // ATTENTION REPETITION
        .attr("class", "linklabel")
        .attr("fill", function(d){                   
          return color(colors.indexOf(d.properties.name)); 
        });
}



function createFeatures(d){

  // Transforme les nodes en points, les links en lines
  var nodesFeatures = [],
      linksFeatures = [];

  d.nodes.forEach(function(e){

    // On ne garde que les noeuds qui sont liés avec d'autres noeuds
    var islinked = isLinked(e, d.links);
    if (islinked){
      var tobeCut = toBeCut(e);
      var feature = {};
      feature['type'] = 'Feature';
      feature['geometry'] = {"type": "Point", 
                              "coordinates": [e.lon - tobeCut.lon, e.lat - tobeCut.lat]};
      feature['properties'] = {"name" : JSON.parse(e.name), 
                               "nb_vald": e.nb_vald};
      nodesFeatures.push(feature);
    }
  });

  d.links.forEach(function(e){

    var source = d.nodes.filter(function(n) {  if(n.name === e.source){
                                              return n; }})[0]
    var target = d.nodes.filter(function(n) {  if(n.name === e.target){
                                              return n; }})[0]
    var sourceTobeCut = toBeCut(source),
        targetTobeCut = toBeCut(target);

    var feature = {};
        feature['type'] = 'Feature';
        feature['geometry'] = {"type": "LineString", 
                               "coordinates": [target.lon - targetTobeCut.lon, target.lat - targetTobeCut.lat, 
                                               source.lon - sourceTobeCut.lon, source.lat - sourceTobeCut.lat]};
        feature['properties'] = {"name" : JSON.parse(e.name), 
                                 "nb_vald": e.total_valds,
                                 "target_name": JSON.parse(target.name),
                                 "source_name": JSON.parse(source.name)};
    linksFeatures.push(feature);

  })

  return {"nodesFeatures": nodesFeatures, "linksFeatures": linksFeatures};
}

// Détermine si un noeud est lié avec d'autres noeuds
function isLinked(n, linksData){

  var islinked = false;
  linksData.forEach(function(u){
      if(u.source.localeCompare(n.name) === 0 || u.target.localeCompare(n.name) === 0){
        islinked = true ;
        return true; // ne renvoie rien... :(
      }
    });
  return islinked;
}

//Création d'un array permettant d'indicier les noms des routes

function arrayColor(d){

  var colors = new Array();
  d.forEach(function(e){
    if(colors.indexOf(e.properties.name) == -1){
      colors.push(e.properties.name);
    }
  })
  return colors ;
}

// Détermine la distance à enlever pour les noeuds en périphérie (afin de les approcher)
function toBeCut(d){

  var chatelet_lat = 48.858612060546875,
      chatelet_lon = 2.3480260372161865;

  var massy_lat = 48.72431564331055,
      massy_lon = 2.258233070373535;

  var tobeCut = {"lon": 0.0, "lat" : 0.0};

    if(Math.pow(d.lat - chatelet_lat,2) + Math.pow(d.lon - chatelet_lon, 2)
       > Math.pow(48.873985290527344 -  chatelet_lat,2) + Math.pow(2.2952630519866943 - chatelet_lon, 2)){
        tobeCut.lon = (d.lon - chatelet_lon)/1.5;
        tobeCut.lat = (d.lat - chatelet_lat)/1.5;
    }
    if(Math.pow(d.lat - chatelet_lat,2) + Math.pow(d.lon - chatelet_lon, 2)
       >= Math.pow(massy_lat -  chatelet_lat,2) + Math.pow(massy_lon - chatelet_lon, 2)){
        tobeCut.lon = (d.lon - chatelet_lon)/1.3;
        tobeCut.lat = (d.lat - chatelet_lat)/1.3;
    }

    return tobeCut ; 
}


//http://plnkr.co/edit/20t4F02vsM1U55ktCv66?p=preview