/**
 *  Insteon Plugin-in Module
 *
 *  Copyright 2016 Jordan Maxwell
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
metadata {
	definition (name: "Insteon Plugin Module", namespace: "thetestgame", author: "Jordan Maxwell") {
		capability "Switch"
        capability "Polling"
		capability "Refresh"
       
        command "beep"
	}
    
    preferences {
        input("InsteonUsername", "text", title: "Insteon Username/Email")
        input("InsteonPassword", "text", title: "Insteon Password")
        input("ApiKey", "text", title: "Insteon Cloud Api Key")
        input("DeviceId", "text", title: "Insteon Device Id")
    }

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
        standardTile("switch", "device.switch", canChangeIcon: false) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.on", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.off", backgroundColor: "#79b821", iconColor: "#ffffff"
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
        	state "default", action:"refresh.refresh", icon: "st.secondary.refresh"
        }
        
        main "switch" 
		details(["switch","refresh"])
	}
}

// parse events into attributes
def parse(String description) {}

def poll() {
	refresh()
}

def refresh() {
	def stats = getStatus() 
    if (stats.status == "succeeded") {
    	def level = stats.response.level
        log.debug level
        if (level > 0) {
        	sendEvent(name: "switch", value: "on")
        } else {
    		sendEvent(name: "switch", value: "off")        
        }
    }
}

def on() {
	log.debug "Executing 'on'"
	sendCommand("fast_on")
    sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "Executing 'off'"
	sendCommand("fast_off")
    sendEvent(name: "switch", value: "off")
}

def beep() {
	log.debug "Executing 'beep'"
    sendCommand("beep")
}

def getOAuth() {
	def token = null
	httpPost(
		uri: "https://connect.insteon.com/api/v2/oauth2/token",
		body: [grant_type: 'password', username: InsteonUsername, password: InsteonPassword, client_id: ApiKey],  
	) { resp->
    	token = resp.data.access_token	
    }
    return token
}

def sendCommand(cmd) {
	def token = getOAuth()
    def response = null
    try {
        httpPostJson(
            uri: "https://connect.insteon.com/api/v2/commands",
            headers: ['Content-Type': 'application/json', 'Authentication': "APIKey ${ApiKey}", 'Authorization': "Bearer ${token}"],
            body: [command: cmd, device_id: DeviceId],  
        ) { resp->
            response = resp.data
        }  
    } catch(e) {
    	log.error "Error: ${e}"
    }
    return response
}

def getStatus() {
	def returning = sendCommand("get_status")
    def check = returning.link
    def response = null
    def token = getOAuth()
    while (response == null) {
        try {
            httpGet(
                uri: "https://connect.insteon.com",
                path: check,
                headers: ['Content-Type': 'application/json', 'Authentication': "APIKey ${ApiKey}", 'Authorization': "Bearer ${token}"],
             ) { resp->
               	 if (resp.data.status != "pending") {
                 	response = resp.data
                	log.debug resp.data.status
                 }
            }
        } catch(e) {
            log.error "Error: ${e}"
        }
    }
    return response
}