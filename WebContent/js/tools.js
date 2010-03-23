function backToTop(first) {
	
    var x1 = x2 = x3 = 0;
    var y1 = y2 = y3 = 0;

    if (document.documentElement) {
        x1 = document.documentElement.scrollLeft || 700;
        y1 = document.documentElement.scrollTop || 700;
    }

    if (document.body) {
        x2 = document.body.scrollLeft || 700;
        y2 = document.body.scrollTop || 700;
    }

    x3 = window.scrollX || 700;
    y3 = window.scrollY || 700;

    var x = Math.max(x1, Math.max(x2, x3));
    var y = Math.max(y1, Math.max(y2, y3));

    window.scrollTo(Math.floor(x / 3), Math.floor(y / 3));
    
    if (x > 700 || y > 700) {
        if(first == true)
        	window.setTimeout("backToTop(false)", 800);
        else
        	window.setTimeout("backToTop(false)", 50);
    }
}