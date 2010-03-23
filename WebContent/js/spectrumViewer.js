/*
 * SPECTRUM VIEWER using Raphael and jQuery
 * by Sebastian Wolf 
 * IPB Halle	
 * 
 */

//set the spectrum size...
var maxX = 800;
var maxY = 200;
var margin = 45;
var marginRight = 20;


//check if array contains a given string
function contains(arryA,str){
  for(var i=0;i<arryA.length;i++){
  	if(arryA[i]===str)
          return true;
  }
  return false;
}


Raphael.fn.line = function (x1, y1, x2, y2) {
    return this.path("M" + x1 + " " + y1 + "L" + x2 + " " + y2);
};


//draw the initial spectrum
function drawSpectrum(maxWidth, maxHeight, containingDiv, mzFound, intensitiesFound, mzNotFound, intNotFound, mzNotUsed, intNotUsed) {
	
	//set width
	maxX = maxWidth;
	maxY = maxHeight;
	
	
	//reset canvas
	jQuery('svg').remove();
	
	
	var translate = Math.round((maxX-margin) / 7.55);
	var tick = 50;
	//one tick has to be in the middle...otherwise the zoom is not working
	var tickMzMaxCount = 11;
	var tickIntMaxCount = 5;

	//measured peaks
	var mz = mzFound.concat(mzNotFound).concat(mzNotUsed);
	var intensities = intensitiesFound.concat(intNotFound).concat(intNotUsed);
	var colors = [];

	var maxMZ = 0;
	var maxIntensity = 0;
	var maxMZI = 0;
	var maxIntensityI = 0;
	//scale axis**********************
	//find maximum
	for(var i = 0; i < intensities.length; i++)
	{
		if(maxIntensity < intensities[i]){
			maxIntensity = intensities[i];
			maxIntensityI = i;
		}

		if(maxMZ < mz[i]){
			maxMZ = mz[i];
			maxMZI = i;
		}
	}

	var scaledMZ = new Array();
	var scaledIntensity = new Array();
	
	for(var i = 0; i < mz.length; i++)
	{
		scaledMZ[i] = (mz[i] * (maxX - (margin + marginRight))) / maxMZ;
		scaledIntensity[i] = Math.round(((intensities[i] * (maxY - margin - 10)) / maxIntensity)* 1000) / 1000;
	}

	// Each of the following examples create a canvas that is 320px wide by 200px high
	var paper = Raphael(containingDiv, maxX, maxY);
	var st = paper.set();
	var stXaxisFont = paper.set();
	var stTranslation = paper.set();
	var stYaxisSet = paper.set();
	
	
	var rectYaxis = paper.rect(0,0,margin, maxY).attr({'fill': '#fff', 'stroke-width' : 0});
	stYaxisSet.push(rectYaxis);
	
	//x and y-axis
	st.push(paper.line(margin, maxY-margin, (maxX - marginRight) + margin, maxY-margin)).attr({"stroke": "black", "stroke-width": 2});
	//y-axis is not to be zoomed!
	stYaxisSet.push(paper.line(margin + 1, maxY - margin + 1, margin + 1, 9).attr({"stroke": "black", "stroke-width": 2}));

	var xAxisLegend = paper.text( maxX / 2 + margin, maxY - margin + 25, "m/z");
	xAxisLegend.attr({'font-family' :'Verdana','fill' :'#acacac'});
	
	var yAxisLegend = paper.text( 40, maxY/2 - 10, "Intensity");
	yAxisLegend.attr({'rotation':'-90,15,' +  (maxY/2),'fill' :'#acacac','font-family' :'Verdana'});
	stYaxisSet.push(yAxisLegend);
	
	//draw the legend on the x-axis
	var scaledTick = Math.round((((maxMZ / tickMzMaxCount) * (maxX - (margin + marginRight))) / maxMZ) * 1000) / 1000;
	var tickCount = Math.ceil(scaledMZ[maxMZI] / scaledTick); 

	//legend on y-axis
	var scaledIntTick = Math.round((((maxIntensity/tickIntMaxCount) * (maxY - margin - 10)) / (maxIntensity)) * 1000) / 1000;
	var tickIntCount = Math.ceil((scaledIntensity[maxIntensityI]) / scaledIntTick);
	
	//x-axis description	
	var legendX = (maxMZ / tickMzMaxCount);
	for ( var i = 1; i < tickCount; i++) {
		st.push(paper.line((scaledTick*i) + margin, maxY - margin, (scaledTick*i) + margin, maxY - margin + 4))
			.attr({"stroke": "black", "stroke-width": 2});

		var xAxisLegend1 = paper.text( (scaledTick*i) + margin, maxY - margin + 10, Math.round(legendX))
								.attr({'fill' :'black', 'font-family' :'Verdana'});
		stXaxisFont.push(xAxisLegend1);
		
		legendX += (maxMZ / tickMzMaxCount);
	}

	//y-axis description
	var legendY = maxIntensity;
	for ( var i = 0; i <= tickIntCount; i++) {

		if(legendY < 0)
			continue;

		stYaxisSet.push(paper.line(margin-4, (scaledIntTick * i) + 10, margin, (scaledIntTick * i) + 10));

		stYaxisSet.push(paper.text( margin - 25, (scaledIntTick * i) + 10, legendY).attr({'fill' :'black','font-family' :'Verdana'}));
		legendY -= Math.ceil((maxIntensity/tickIntMaxCount));
	}
	
	var test;
	var peaks = new Array();
	var leave_timer;
	var frame = paper.rect(10, 10, 100, 45, 5).attr(
			{fill: "#efefef", stroke: "#000", "stroke-width": 2, "fill-opacity": 0.6, "stroke-opacity": 0.4}
	).hide();
//	var image = paper.image("http://localhost:8080/MetFragWeb/FragmentPicsMzAnno/A78B8A38167EE07AA1106F19C251C740/MzAnnotPics/mzAnnoMeasured_0.png", margin + 100, 0, 200, 200);
//	image.toBack();
//	image.hide();
	
	
	
	var is_label_visible = false;
	var label = [];
	var txt = {"font": '11px "Verdana"', stroke: "none", fill: "#000"};
	label[0] = paper.text(60, 30, "").attr(txt).hide();
	
	
	
	
	//draw the peaks into the diagram
	for ( var i = 0; i < mz.length; i++) {
		var temp = 0;
		if (scaledIntensity[i] < 10)
			temp = (maxY - margin) - scaledIntensity[i];
		else
			temp = (maxY - (margin)) - (scaledIntensity[i]);

		//color of the not found peak
		var color = "#CC0000";
		var explainStr = "Not Found!";
		if(contains(mzFound, mz[i]))
		{
			color = "#006600";
			explainStr = "Found!";
		}
		else if(contains(mzNotUsed, mz[i]))
		{
			color = "#008AB8";
			explainStr = "Not Used!";
		}		

		peaks = paper.line(scaledMZ[i] + margin, maxY - margin, scaledMZ[i] + margin, temp).attr({
			"stroke": color, 
			"stroke-width": 2,
			"class": "peak",
			"id": i});
		st.push(peaks);
		
		(function (x, y, data, lbl, line, color, explainStr) {
			var timer = 0;
			jQuery(peaks[0]).hover(function () {
				clearTimeout(leave_timer);
				var newcoord = {x: maxX - 100, y: 0};
				if (newcoord.x + 100 > maxX) {
					newcoord.x = maxX - 205;
				}
				if (newcoord.y - 10 < 0) {
					newcoord.y = 3;
				}	
//				image.show();
				frame.show().toFront().animate({x: newcoord.x, y: newcoord.y}, 100 * is_label_visible);
				label[0].toFront().attr({text: "m/z: " + data + "\nintensity: " + lbl + "\n" + explainStr}).show().animate({x: newcoord.x * 1 + 50, y: newcoord.y * 1 + 20}, 100 * is_label_visible);
				line.attr("stroke", "orange");
				is_label_visible = true;
				paper.safari();
			}, function () {
				paper.safari();
				line.attr("stroke", color);
				leave_timer = setTimeout(function () {
					frame.hide();
//					image.hide();
					label[0].hide();
					is_label_visible = false;
					paper.safari();
				}, 500);
			});
		})(scaledMZ[i], temp, mz[i], intensities[i], peaks, color, explainStr);
	}
	
	//only 1 zoom mode for now
	var translations = 0;
	var zoom = 1;
	var zoomin = paper.image("./images/magnify.png", 80, 10, 32, 32);
	var right = paper.image("./images/right.png", 110, 10, 32, 32);
	var left = paper.image("./images/left.png", 50, 10, 32, 32);
	left.hide();
	right.hide();
	
	zoomin.node.onclick = function () { 
		
		if(translations != 0)
		{
			//stTranslation.animate({translation: translations + ",0"}, 100);
			stTranslation.translate((translations * translate),0);
			translations = 0;
		}
		
		if(zoom == 1)
		{
			st.animate({scale: "2,1," + ((maxX/2) + margin) + "," + (maxY/2)}, 300);
			stYaxisSet.toFront();
			stTranslation.toBack();
//			image.toBack();
			left.show();
			right.show();
		}
		else
		{
			st.animate({scale: "1,1," + ((maxX/2) + margin) + "," + (maxY/2)}, 300);
			left.hide();
			right.hide();
		}
		
		//now move the legend
		var mid = Math.floor(stXaxisFont.length / 2);
		var tempCount = 5;
		var tempTranslation = scaledTick * tempCount;
		var hasPassedMid = false;
		for (var i = 0; i < stXaxisFont.length; i++) {
			if(i < mid)
				stXaxisFont[i].translate((-tempTranslation * zoom), 0);
			else if(i == mid)
			{
				stXaxisFont[i].translate(0, 0);
				hasPassedMid = true;
			}
			else
				stXaxisFont[i].translate(tempTranslation * zoom, 0);
			
			//now set temp translation
			if(hasPassedMid)
			{
				tempCount++;
				tempTranslation = (scaledTick) * tempCount;
			}
			else if (tempCount > 0)
			{
				tempCount--;
				tempTranslation = (scaledTick) * tempCount;
			}
			else
				tempTranslation = (scaledTick);
		}
		
		//zoom in or out again!
		if(zoom == 1)
			zoom = -1;
		else
			zoom = 1;
	};
	
	//translation	
	//put every part in one group
	stTranslation.push(st);
	stTranslation.push(stXaxisFont);
	
	//left.attr("fill", "green");
	left.node.onclick = function () { 
		//not zoomed in....no need for any translation
		if(zoom == -1 && translations > -4)
		{
			stTranslation.translate(translate,0);
			//stTranslation.animate({translation: "100,0"}, 100);
			translations--;
			right.show();
		}
		else if(translations == -4)
		{
			left.hide();
		}			
	};
	
	
	right.node.onclick = function () { 
		//not zoomed in....no need for any translation
		if(zoom == -1 && translations < 4)
		{
			stTranslation.translate(-translate,0);
			//stTranslation.animate({translation: "-100,0"}, 100);
			translations++;
			left.show();
		}
		else if(translations == 4)
		{
			right.hide();
		}
			
	};
	
	
}
