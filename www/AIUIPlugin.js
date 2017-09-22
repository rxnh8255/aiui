
var AIUIPlugin = function() {};

AIUIPlugin.prototype.start = function(success, fail) {
	return cordova.exec(success, fail, "AIUIPlugin", "start", [{}]);
};

AIUIPlugin.prototype.stop = function(success, fail) {
	return cordova.exec(success, fail, "AIUIPlugin", "stop", [{}]);
};
AIUIPlugin.prototype.registerNotify = function(success, fail) {
	return cordova.exec(success, fail, "AIUIPlugin", "registerNotify", [{}]);
};
AIUIPlugin.prototype.startText = function(message,success, fail) {
	return cordova.exec(success, fail, "AIUIPlugin", "stop", [{text:message}]);
};

window.aiuiPlugin = new AIUIPlugin();