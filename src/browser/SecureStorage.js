var SecureStorageProxy = {

	init: function(win, fail, args) {
		setTimeout(win, 0);
	},

	get: function(win, fail, args) {
		try {
			var service = args[0];
			var key = args[1];
			win(window.localStorage.getItem("SecureStorage_" + service + "_" + key));
		} catch (e) {
			fail('Failure in SecureStorage.get() - ' + e.message);
		}
	},

	set: function(win, fail, args) {
		try {
			var service = args[0];
			var key = args[1];
			var value = args[2];

			window.localStorage.setItem("SecureStorage_" + service + "_" + key, value);

			win(key);
		} catch (e) {
			fail('Failure in SecureStorage.set() - ' + e.message);
		}
	},

	remove: function(win, fail, args) {
		try {
			var service = args[0];
			var key = args[1];

			window.localStorage.removeItem("SecureStorage_" + service + "_" + key);

			if (passwordCredential) {
				vault.remove(passwordCredential);
			}

			win(key);
		} catch (e) {
			fail('Failure in SecureStorage.remove() - ' + e.message);
		}
	},

	keys: function(win, fail, args) {
		try {
			var service = args[0];

			var passwordCredentials = [];

			for (var i = 0; i < window.localStorage.length; i++) {
				var key = window.localStorage.key(i);
				if (key.indexOf("SecureStorage_" + service) === 0) {
					passwordCredentials.push(key);
				}
			}

			win(passwordCredentials);
		} catch (e) {
			fail('Failure in SecureStorage.keys() - ' + e.message);
		}
	},

	clear: function(win, fail, args) {
		try {
			var service = args[0];

			for (var i = window.localStorage.length - 1; i >= 0; i--) {
				var key = window.localStorage.key(i);
				if (key.indexOf("SecureStorage_" + service) === 0) {
					window.localStorage.removeItem(key);
				}
			}

			win();
		} catch (e) {
			fail('Failure in SecureStorage.clear() - ' + e.message);
		}
	},
};

require("cordova/exec/proxy").add("SecureStorage", SecureStorageProxy);