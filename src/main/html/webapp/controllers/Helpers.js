can.ejs.Helpers.prototype.prettyState = function(state) {

	if (this.state == 1) {
		return 'Waiting';
	} else if (this.state == 2) {
		return 'Running';
	} else if (this.state == 3) {
		return 'Exporting Data';
	} else if (this.state == 4) {
		return 'Complete';
	} else if (this.state == 5) {
		return 'Error';
	} else if (this.state == 6) {
		return 'Canceled';
	} else {
		return 'Error';
	}

};

can.ejs.Helpers.prototype.prettyTime = function(start, end, current) {

	if (start === 0 && end === 0) {
		return '-';
	}

	if (start > 0 && end === 0){
		executionTime = current - start;
	}else{
		executionTime =end - start;
	}



	if (executionTime <= 0) {

		return '-';

	} else {

		var h = (Math.floor((executionTime / 1000) / 60 / 60));
		var m = ((Math.floor((executionTime / 1000) / 60)) % 60);

		return (h > 0 ? h + ' h ' : '') + (m > 0 ? m + ' min ' : '')
				+ ((Math.floor(executionTime / 1000)) % 60) + ' sec';

	}

};

can.ejs.Helpers.prototype.prettyDate = function(unixTimestamp) {

	if (unixTimestamp > 0) {
		var dt = new Date(unixTimestamp);
		return dateFormat(dt, "default");
	} else {
		return '-';
	}

};

String.prototype.endsWith = function (s) {
		return this.length >= s.length && this.substr(this.length - s.length) == s;
};
