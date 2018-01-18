/**
 *  Generic HTTP Device v1.0.20160402
 *
 *  Source code can be found here: https://github.com/LF-SmartThings/
 *  Copyright 2018 LF
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

import groovy.json.JsonSlurper

metadata {
	definition (name: "LF_GarageDoor", author: "lf", namespace:"") {
        attribute "status", "string"
   		capability "Polling"

		capability "Actuator"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Sensor"

		command "openG1"
		command "closeG1"
		command "openG2"
		command "closeG2"
		command "open"
		command "status"
	}


	preferences {
		input("DeviceIP", "string", title:"Device IP Address", description: "Please enter your device's IP Address", required: true, displayDuringSetup: true)
		input("DevicePort", "string", title:"Device Port", description: "Please enter port 80 or your device's Port", required: true, displayDuringSetup: true)
		input("DevicePath", "string", title:"URL Path", description: "Rest of the URL, include forward slash. like '/', /abc/", displayDuringSetup: true)
		input(name: "DevicePostGet", type: "enum", title: "POST or GET", options: ["POST","GET"], required: true, displayDuringSetup: true)
		input("DeviceBodyText", "string", title:'Body Content', description: 'Type in "GateTrigger=" or "CustomTrigger="', required: true, displayDuringSetup: true)
		input("UseJSON", "bool", title:"Use JSON instead of HTML?", description: "Use JSON instead of HTML?", defaultValue: false, required: false, displayDuringSetup: true)
		section() {
			input("HTTPAuth", "bool", title:"Requires User Auth?", description: "Choose if the HTTP requires basic authentication", defaultValue: false, required: true, displayDuringSetup: true)
			input("HTTPUser", "string", title:"HTTP User", description: "Enter your basic username", required: false, displayDuringSetup: true)
			input("HTTPPassword", "string", title:"HTTP Password", description: "Enter your basic password", required: false, displayDuringSetup: true)
		}
    }

	simulator {
	}

	tiles {
		standardTile("refresh", "command.refresh", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"polling.poll", icon:"st.secondary.refresh"
		}

		standardTile("toggle1", "gATE1", width: 2, height: 2) {
			state("closed", label:'${name}' + " 1", action:"openG1", icon:"st.doors.garage.garage-closed", backgroundColor:"#00A0DC", nextState:"opening")
			state("open", label:'${name}' + " 1", action:"closeG1", icon:"st.doors.garage.garage-open", backgroundColor:"#e86d13", nextState:"closing")
			state("opening", label:'${name}' + " 1", icon:"st.doors.garage.garage-closed", backgroundColor:"#e86d13")
			state("closing", label:'${name}' + " 1", icon:"st.doors.garage.garage-open", backgroundColor:"#00A0DC")
			state("error", label: 'Failed', action: "", backgroundColor: "#FFAA33")
			state("trying", label: 'TRYING', action: "", backgroundColor: "#FFAA33")		
		}
		standardTile("open1", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'open door 1', action:"openG1", icon:"st.doors.garage.garage-opening"
		}
		standardTile("close1", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'close door 1', action:"closeG1", icon:"st.doors.garage.garage-closing"
		}


		standardTile("toggle2", "gATE2", width: 2, height: 2) {
			state("closed", label:'${name}' + " 2", action:"openG2", icon:"st.doors.garage.garage-closed", backgroundColor:"#00A0DC", nextState:"opening")
			state("open", label:'${name}' + " 2", action:"closeG2", icon:"st.doors.garage.garage-open", backgroundColor:"#e86d13", nextState:"closing")
			state("opening", label:'${name}' + " 2", icon:"st.doors.garage.garage-closed", backgroundColor:"#e86d13")
			state("closing", label:'${name}' + " 2", icon:"st.doors.garage.garage-open", backgroundColor:"#00A0DC")
			state("error", label: 'Failed', action: "", backgroundColor: "#FFAA33")
			state("trying", label: 'TRYING', action: "", backgroundColor: "#FFAA33")
		}
		standardTile("open2", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'open door 2', action:"openG2", icon:"st.doors.garage.garage-opening"
		}
		standardTile("close2", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'close door 2', action:"closeG2", icon:"st.doors.garage.garage-closing"
		}
        
		main (["toggle1", "toggle2"])
		details(["toggle1", "open1", "close1", "toggle2", "open2", "close2", "refresh"])
	}
    
}

def updated() {
	log.debug "----------------update called"
    //return
	unschedule()
	runEvery1Minute(refresh)
	//runEvery5Minutes(refresh)
	runIn(2, refresh)	//after 2 second take a run for refresh()
}
def poll() {
	log.debug "Executing 'poll'"
	log.debug "do nothing on 'poll'"
	//status()
	//parent.poll()
}
def refresh() {
	log.debug "---------------refresh"
    return
    status();
}

def onoffGate(int gid) {
	log.debug "Toggle gate $gid"
    if (gid == -1) {
		runCmd("open?gid=-1")
    } else {
    	runCmd("open?gid=${gid}")
    }
}

def open() {
	onoffGate(-1)
}

def openG1() {
	onoffGate(1)
	//log.debug "Triggered OPEN!!!"
	////sendEvent(name: "triggerswitch", value: "triggeron", isStateChange: true)
    //state.blinds = "on";
	//runCmd("open")
}
def closeG1() {
	onoffGate(1)
	//log.debug "Triggered CLOSE!!!"
	////sendEvent(name: "triggerswitch", value: "triggeroff", isStateChange: true)
    //state.blinds = "off";
	//runCmd("close")
}

def openG2() {
	onoffGate(2)
}
def closeG2() {
	onoffGate(2)
}

def status() {
	log.debug "get status"
	sendEvent(name: "toggle1", value: "trying", isStateChange: true)
	sendEvent(name: "toggle2", value: "trying", isStateChange: true)
	runCmd("status")	
}

def runCmd(String varCommand) {
	def host = DeviceIP
    def hosthex = convertIPtoHex(host).toUpperCase()
	def porthex = convertPortToHex(DevicePort).toUpperCase()
	device.deviceNetworkId = "$hosthex:$porthex"
	def userpassascii = "${HTTPUser}:${HTTPPassword}"
	def userpass = "Basic " + userpassascii.encodeAsBase64().toString()

	//log.debug "The device id configured is: $device.deviceNetworkId"

	//def path = DevicePath
	def path = DevicePath + varCommand
	log.debug "path is: $path"
	//log.debug "Uses which method: $DevicePostGet"
	def body = ""//varCommand
	log.debug "body is: $body"

	def headers = [:]
	headers.put("HOST", "$host:$DevicePort")
	headers.put("Content-Type", "application/x-www-form-urlencoded")
	if (HTTPAuth) {
		headers.put("Authorization", userpass)
	}
	log.debug "The Header is $headers"
	def method = "POST"
	try {
		if (DevicePostGet.toUpperCase() == "GET") {
			method = "GET"
			}
		}
	catch (Exception e) {
		settings.DevicePostGet = "POST"
		log.debug e
		log.debug "You must not have set the preference for the DevicePOSTGET option"
	}
	log.debug "The method is $method"
	try {
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: path,
			body: body,
			headers: headers
			)
		hubAction.options = [outputMsgToS3:false]
		//log.debug hubAction
		hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	}
}

def parse(String description) {
	//log.debug "Parsing '${description}'"
	def whichTile = ''	
	//log.debug "state.blinds " + state.blinds
    
    def msg = parseLanMessage(description)
	def headersAsString = msg.header // => headers as a string
	def headerMap = msg.headers      // => headers as a Map
	def body = msg.body              // => request body as a string
	def status = msg.status          // => http status code of the response
	def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
	def xml = msg.xml                // => any XML included in response body, as a document tree structure
	def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)
	log.debug "Receiving Message From Device: " + status + " body: " + body
	log.debug "JSON: " + json

	int sensor_state
	if (json.containsKey("sensor_state")){
		log.debug "sensor_state: " + json.sensor_state
    	sensor_state = json.sensor_state
	} else {
    	sensor_state = 0
    }
	if (json.containsKey("gate_states")){
		log.debug "gate_state: " + json.gate_states
	}
    
    def gate1 = doorStatus((sensor_state>>2)&0x3)
	log.debug "gate_1: " + gate1

    def gate2 = doorStatus((sensor_state)&0x3)
	log.debug "gate_2: " + gate2
    
    int state1 = (sensor_state>>2)&0x3
    int state2 = (sensor_state)&0x03
    
    switch(state1) {
    	case 0:	//error, out of power?
			sendEvent(name: "gATE1", value: "error", isStateChange: true)
			//def result = createEvent(name: "switch", value: "off", isStateChange: true)
			//return result
        	break
    	case 1:	//closed
			sendEvent(name: "gATE1", value: "closed", isStateChange: true)
        	break
    	case 2:	//open
			sendEvent(name: "gATE1", value: "open", isStateChange: true)
        	break
    	case 3:	//middle, moving...
			sendEvent(name: "gATE1", value: "trying", isStateChange: true)
        	break
    }

    switch(state2) {
    	case 0:	//error, out of power?
			sendEvent(name: "gATE2", value: "error", isStateChange: true)
			//def result = createEvent(name: "switch", value: "off", isStateChange: true)
			//return result
        	break
    	case 1:	//closed
			sendEvent(name: "gATE2", value: "closed", isStateChange: true)
        	break
    	case 2:	//open
			sendEvent(name: "gATE2", value: "open", isStateChange: true)
        	break
    	case 3:	//middle, moving...
			sendEvent(name: "gATE2", value: "trying", isStateChange: true)
        	break
    }

	//if (json.containsKey("state") && json.state == 1) {
    //    whichTile = 'mainon'
    //}
    //if (json.containsKey("state") && json.state == 0) {
    //    whichTile = 'mainoff'
    //}
    
    /*switch (whichTile) {
        case 'mainon':
			sendEvent(name: "triggerswitch", value: "triggeron", isStateChange: true)
			def result = createEvent(name: "switch", value: "on", isStateChange: true)
			return result
        case 'mainoff':
			sendEvent(name: "triggerswitch", value: "triggeroff", isStateChange: true)
			def result = createEvent(name: "switch", value: "off", isStateChange: true)
			return result
        default:
			def result = createEvent(name: "testswitch", value: "default", isStateChange: true)
			//log.debug "testswitch returned ${result?.descriptionText}"
			return result
    }*/
}

private String convertIPtoHex(ipAddress) {
	String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	//log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
	return hex
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	//log.debug hexport
	return hexport
}
private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
private String convertHexToIP(hex) {
	//log.debug("Convert hex to ip: $hex")
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	//log.debug device.deviceNetworkId
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

private String doorStatus(int ocValue) {
  String state;
  switch (ocValue) {
    case 0:
      state = "error";
      break;
    case 1:
      state = "closed";
      break;
    case 2:
      state = "opened";
      break;
    case 3:
      state = "moving...";
      break;
    default:
      state = "something wrong!";
      break;
  }
  return state;
}