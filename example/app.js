const win = Titanium.UI.createWindow()
win.open();

const car = require("ti.car");
car.createListTemplate({
	title: "ti.car demo",
	sections: [{
		title: "List section",
		items: [{
				text: "Titanium SDK"
			},
			{
				text: "Android Auto"
			},
			{
				text: "TDemo"
			}
		]
	}]
});

car.addEventListener("click", function(ev) {
	alert(ev.index);
	car.createToast("You clicked: " + ev.index);
})
