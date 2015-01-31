
var chatLog = [];
var userList =[];
var stompClient;
var EnvTab = React.createClass({
	getVariables: function()
	{
		var results;
		$.ajax({
			  contentType: "application/json",
			  type: "GET",
			  url: "variables",
			  async: false,
			  success: function(data) {
			  results = data;
			  }		 	
			});
		return results;
	},
	render: function() {
		var variableList = this.getVariables().map(function(row) {
			return (<tbody><tr><td>{row}</td></tr></tbody>);
		});
		return (
		<div>
		<table className="table table-hover table-bordered">
			{variableList}
		</table>
		</div>
		);
	}
})
var MediaTab = React.createClass({
	render: function() {
		
		var plots = chatLog.map(function(row) {
			if (row.type =="Rplot") {
				return(
						<tbody><tr className="info"><td className="charuserbox"><div><span className="glyphicon glyphicon-user"/>{row.userId}</div></td><td><a href={"plots/"+row.message} rel="lightbox"><img id="chatimg" src={"plots/"+row.message}/></a></td></tr></tbody>
						);
			}
		});
		
		return (
			<div>
			<table className="table table-hover table-bordered">			
				{plots}
			</table>
			</div>
		);
	}
})
var UserTab = React.createClass({
	render: function() {
		
		var userNode = userList.map(function (row) {
			return(
				<tbody><tr><td>{row}</td></tr></tbody>
				);
		}); 
		
		return (
		<div>
			<table className="table table-hover table-bordered">
				{userNode}
			</table>
		</div>
		);
	}
})

var UserWindow = React.createClass({
	updateUserList: function(list) {
		userList = list;
		console.log(list);
		if (this.state.tab == "user")
			{
				this.forceUpdate();
			}
	},
	getInitialState: function() {
		return {
				tab: "user"
		};
	},
	tabChange: function(tabType) {
		this.setState({tab: tabType});
	},
	render: function()
	{
		var currentTab = function(tab) {
										switch(tab)
										{
										case "user":
											return (<UserTab />);
											break;
										case "env":
											return (<EnvTab />);
											break;
										case "media":
											return (<MediaTab />);
											break;
											
										}
									}
		return(
				<div id="userWindow">
					<div className="row">
						<ul className="nav nav-tabs">
						  <li role="presentation" className={(this.state.tab == 'user')?'active':''}><a href="#" onClick={function(){this.tabChange("user")}.bind(this)} >User</a></li>
						  <li role="presentation" className={(this.state.tab == 'env')?'active':''}><a href="#" onClick={function(){this.tabChange("env")}.bind(this)}>Variables</a></li>
						  <li role="presentation" className={(this.state.tab == 'media')?'active':''}><a href="#" onClick={function(){this.tabChange("media")}.bind(this)}>Media</a></li>
						</ul>
					</div>
					<div className="row">
						{currentTab(this.state.tab)}
					</div>
				</div>
			);
	}
	});
var ChatWindow = React.createClass({
render: function()
{
	
	
	var commentNodes = chatLog.map(function (row) {
		//show image
		if (row.type =="Rplot") { 
			return (
					<tbody><tr className="info"><td className="charuserbox"><div><span className="glyphicon glyphicon-user"/>{row.userId}</div></td><td><a href={"plots/"+row.message} rel="lightbox"><img id="chatimg" src={"plots/"+row.message}/></a></td></tr></tbody>
					);
		}
		else 
		{
			return(
				<tbody>
					<tr className={(row.type=="R")?"info":""}>
						<td className="charuserbox">
							<div>
								<span className="glyphicon glyphicon-user"/>{row.userId}
							</div>
						</td>
						<td>
							{(row.message).split('\n').map(function(row){
								return (<p>{row}</p>);
							})}
						</td>
					</tr>
				</tbody>
				);
		}
	}); 
	
	return (
		<div id="chatwindow" ref="chatwindow" bodyless>
			<table className="table table-hover table-bordered">			
				{commentNodes}
			</table>
		</div>
		);
}
});

var ChatBox
 = React.createClass({
componentDidMount: function()
{
	this.refs.textbox1
			.getDOMNode()
			.addEventListener('keypress', function(e) {
				if (e.keyCode == 13){
					this.props.onUserInput({"message":this.refs.textbox1.getDOMNode().value.trim()});
					this.refs.textbox1.getDOMNode().value = "";}}.bind(this)
					);
},  
handleClick: function() {
this.props.onUserInput({"message":this.refs.textbox1.getDOMNode().value.trim()});
this.refs.textbox1.getDOMNode().value = "";
},
render: function()
{
	return (
		<div className="row" id="userinput">
			<div className="input-group">
		      <div className="input-group-btn dropup">
		        <button type="button" className="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">{(this.props.messageType=="Chat")?"Chat":"R"}<span className="caret" id="dropUpCaret"></span></button>
		        <ul className="dropdown-menu" role="menu">
		          <li><a href="#" onClick={function(){this.props.onMessageType("R")}.bind(this)}>{(this.props.messageType=="R")?<b>{'R'}</b>:"R"}</a></li>
		          <li><a href="#" onClick={function(){this.props.onMessageType("Chat")}.bind(this)}>{(this.props.messageType=="Chat")?<b>{'Chat'}</b>:"Chat"}</a></li>        
		        </ul>
		      </div>
		      <input type="text" ref="textbox1" className="form-control" id="textbox1"/>

		    </div>
				

	

			
		</div>
		);
}
});

var MainBox
 = React.createClass({
componentDidMount: function() {
		connect(this);
},
getInitialState: function() {
	return {
			type: "Chat"
	};
},
handleUserInput: function(input) {
 if (this.state.type == "R")
	 {	
	 	input["type"] = "R";
	 	input["userId"] = userId;
		$.ajax({
		  contentType: "application/json",
		  type: "POST",
		  url: "sendmessage",
		  data: JSON.stringify(input),
		  success: function(results) {console.log("R Message received via POST");}.bind(this)
		 	
		});
	 }
 else
	 {
	 	
	 	input["type"] = "Chat";
	 	stompClient.send("/app/moor", {}, JSON.stringify(input));
	 }
},
responseRecieved: function(result) {
	chatLog.push(result);
	this.forceUpdate();
},
changeMessageType: function(messageType)
{
	this.setState({type:messageType});
},
render: function()
{
	return (
			<div className="mainrow">
				<div className="row mainrow">
					<div className="col-md-8 maincol">
						<ChatWindow />	
					</div>
					<div className="col-md-4 maincol">
						<UserWindow ref="userWindow" />
					</div>
				</div>
				<div className="row">
					<div className="col-md-8 chat">
							<ChatBox onUserInput={this.handleUserInput} onMessageType={this.changeMessageType} messageType={this.state.type} />
					</div>
				</div>
			</div>
		);
}
});

var mainComponent = React.render(<MainBox />, document.getElementById('main'));
