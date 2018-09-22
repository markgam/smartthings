/**
 *  MimoLite Garage Door Opener and Sensor
 *
 *  Author: mb323@me.com
 *  Date: 2015-01-03
 */
metadata {
	definition (name: "MimoLite Garage Door Opener and Sensor", namespace: "mb323", author: "mb323") {
		capability "Door Control"
		capability "Contact Sensor"
		capability "Momentary"
        capability "Switch"
		capability "Refresh"

        //0 0 0x1000 0 0 0 9 0x72 0x86 0x71 0x30 0x31 0x35 0x70 0x85 0x25
        fingerprint deviceId:"0x1000", inClusters:"0x72, 0x86, 0x71, 0x30, 0x31, 0x35, 0x70, 0x85, 0x25"
    }

	// simulator metadata
	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"

        // reply messages
		reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
		reply "200100,delay 100,2502": "command: 2503, payload: 00"

		// status messages
		status "open":  "command: 2001, payload: FF"
		status "closed": "command: 2001, payload: 00"
	}

	// tile definitions
	tiles {
	    standardTile("contact", "device.contact", width: 2, height: 2, canChangeIcon: true) {
			state("unknown", label:'${name}', action:"refresh.refresh", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821", nextState:"opening")
			state("open", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e", nextState:"closing")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
		}
		standardTile("refresh", "device.contact", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main ("contact")
		details(["contact", "refresh"])
	}
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x30: 1, 0x70: 1])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText}"
	return result
}

def sensorValueEvent(Short value) {
	if (value) {
		createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	} else {
		createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd)
{
	sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	[name: "switch", value: cmd.value ? "open" : "closed", type: "physical"]
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd) {
	[name: "switch", value: cmd.value ? "open" : "closed", type: "digital"]
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def push() {
	zwave.basicV1.basicSet(value: 0xFF).format()
}

def open() {
	push()
}

def close() {
	push()
}

def on() {
	push()
}

def off() {
	push()
}

def refresh() {
	zwave.switchBinaryV1.switchBinaryGet().format()
}