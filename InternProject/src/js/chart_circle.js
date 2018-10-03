const margin = {top: 20, right: 20, bottom: 90, left: 120},
      width = 800 - margin.left - margin.right,
      height = 500 - margin.top - margin.bottom;

var x = d3.scaleLinear()
.domain([0,4000])
.rangeRound([0,width-30])

var y = d3.scaleLinear()
.domain([500,0])
.rangeRound([0,height-30]);

var xAxis = d3.axisBottom(x)
.tickSize(1);

var yAxis = d3.axisLeft(y)
.tickSize(1);

var canvas = d3.select("body")
.append("svg") 
.attr("width", width+20) 
.attr("height", height+20)
.append('g')
.attr('transform', 'translate(30,30)');

canvas.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(0," + (height-30) + ")")
    .call(xAxis);
canvas.append("g")
    .attr("class", "y axis")
    .call(yAxis);
var svg=d3.select("body").select("g");
var couleur=["#33cc33","#0066cc","#3333cc","#ff9966","#ff0000"]
var xhttp = new XMLHttpRequest();
xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
        var result=JSON.parse(this.responseText)
        result.forEach(function(element){
            var b=document.createElement("option");
            b.setAttribute("value",element);
            b.innerHTML=element;
            document.getElementsByTagName('select')[0].appendChild(b);
            document.getElementsByTagName('select')[0].setAttribute("onchange","graph()")
        });
    }
};
xhttp.open("GET", "http://localhost:8080/Town/listinsee", true);
xhttp.send();
var bool=false;
function clear(){
    for (var j = 0; j < 5; j++) {
        var element=document.getElementsByClassName("ellipse"+j)[0].remove();
    }
}
function graph(){
    if(bool==true){
        clear();
    }
    bool=true;
    var insee= document.getElementsByTagName('select')[0].value;
    var svg=d3.select("body").select("g");
    var couleur=["#33cc33","#0066cc","#3333cc","#ff9966","#ff0000"]
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var result=JSON.parse(this.responseText)
            for (var i = 0; i < 5; i++) {
                svg.append("ellipse")
                    .attr("class","ellipse"+i)
                    .attr("fill","none")
                    .attr("stroke",couleur[i])
                    .attr("rx",x(result.road[i]))
                    .attr("ry",y(500-result.vald[i]))
                    .attr("cx",x(result.moyenne_road[i]))
                    .attr("cy",y(result.moyenne_vald[i]));
            }
        }

    };
    xhttp.open("GET", "http://localhost:8080/Town/test?insee="+insee, true);
    xhttp.send();
}
