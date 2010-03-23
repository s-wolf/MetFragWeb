jQuery("a.tooltip").live('mouseover', function(e){
/* CONFIG */
var xOffset = 0;
var yOffset = 380;
/* END CONFIG */
this.t = this.title;
this.title = "";
jQuery("body").append("<p id='tooltip'>"+ this.t +" <img src='"+ this.href +"' alt='Image preview' /></p>");
var tipWidth = (jQuery("#tooltip").width() / 2) + 10 ;
jQuery("#tooltip")
.css("top",(e.pagex - xOffset) + "px")
.css("left",(e.pageY + yOffset) + "px")
.css("margin-left", "-"+ tipWidth +"px")
.fadeIn(100, function() {

});
jQuery(this).mousemove(function(e){
jQuery("#tooltip")
.css("top",(e.pageY - yOffset) + "px")
.css("left",(e.pageX + xOffset) + "px")
});
});
jQuery("a.tooltip").live('mouseout', function(e) {
this.title = this.t;
jQuery(this).die("mousemove", "mouseout");
jQuery("#tooltip").remove();
}); 