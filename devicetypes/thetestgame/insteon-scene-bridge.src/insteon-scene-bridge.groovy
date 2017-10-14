/**
 *  Insteon Scene Bridge
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
	definition (name: "Insteon Scene Bridge", namespace: "thetestgame", author: "Jordan Maxwell") {
		capability "Actuator"
		capability "Polling"
		capability "Switch"

        command "beep"
	}
    
    preferences {
        input("InsteonUsername", "text", title: "Insteon Username/Email")
        input("InsteonPassword", "text", title: "Insteon Password")
        input("ApiKey", "text", title: "Insteon Cloud Api Key")
        input("SceneId", "text", title: "Insteon Scene Id")
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
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute

}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
	// TODO: handle 'poll' command
}

def on() {
	log.debug "Executing 'on'"
	sendCommand("on")
}

def off() {
	log.debug "Executing 'off'"
	sendCommand("off")
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
    log.debug token
    try {
        httpPostJson(
            uri: "https://connect.insteon.com/api/v2/commands",
            headers: ['Content-Type': 'application/json', 'Authentication': "APIKey ${ApiKey}", 'Authorization': "Bearer ${token}"],
            body: [command: cmd, scene_id: SceneId],  
        ) { resp->
            response = resp.data
        }  
    } catch(e) {
    	log.error "Error: ${e}"
    }
    return response
}