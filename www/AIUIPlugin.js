
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
AIUIPlugin.prototype.finish = function(success, fail) {
	return cordova.exec(success, fail, "AIUIPlugin", "finish", [{}]);
};
AIUIPlugin.prototype.startText = function(message,success, fail) {
	return cordova.exec(success, fail, "AIUIPlugin", "startText", [{text:message}]);
};
AIUIPlugin.prototype.ttsPlay = function(message,success, fail) {
	return cordova.exec(success, fail, "AIUIPlugin", "ttsPlay", [{text:message}]);
};
AIUIPlugin.prototype.registFamily = function(familyId,success, fail) {
	return cordova.exec(success, fail, "AIUIPlugin", "registFamily", [{familyId:familyId}]);
};

window.aiuiPlugin = new AIUIPlugin();