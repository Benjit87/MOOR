var userId = "user" + Math.floor(Math.random()*10000);
var socketHeader = {"userId" : userId};
function connect(main) {

var socket = new SockJS('/moor');
    stompClient = Stomp.over(socket);            
stompClient.connect(socketHeader, function(frame) {
	stompClient.subscribe('/topic/mainroom', function(response){
        main.responseRecieved(JSON.parse(response.body));
 
    });
	
	stompClient.subscribe('/topic/userlist', function(response){
        main.refs.userWindow.updateUserList(JSON.parse(response.body));
    });
	
	console.log('Connected: ' + frame);
});


socket.onclose = function() {
    console.log('close');
    stompClient.disconnect();
};

}

//stompClient.send("/app/hello", {}, JSON.stringify({ 'message': 'test' }));


//stompClient.send("/app/moor", {'test':'test'}, JSON.stringify({ 'name': 'test' }));
