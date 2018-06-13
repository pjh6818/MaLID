const express = require('express');
const app = express();
var fs = require('fs');
var PythonShell = require('python-shell');
var mysql = require('mysql');
var pool = mysql.createPool({
	host: 'malid.cgvtbxiixr2x.ap-northeast-2.rds.amazonaws.com',
	user: 'MALID',
	database: 'MALID',
	password: '1234qwer'
});


var options = {
  mode: 'text',
  pythonPath: '',
  pythonOptions: ['-u'],
  scriptPath: '',
  args: ''
};

function leadingZeros(n, digits) {
  var zero = '';
  n = n.toString();

  if (n.length < digits) {
    for (i = 0; i < digits - n.length; i++)
      zero += '0';
  }
  return zero + n;
}

app.post('/post', (req, res) => {
   var jsonData ="";
   var XYZ_str="";
   var res_str="";
   var inputData="";
   var d = new Date();
   var date = ""+d.getFullYear()+(d.getMonth()+1)+d.getDate();
   var time = leadingZeros(d.getHours(), 2) + ':' +leadingZeros(d.getMinutes(), 2) + ':' +leadingZeros(d.getSeconds(), 2);
   
   req.on('data', (data) => {
   	try{
		  jsonData += data;
   	}catch(e){
		  console.log('Wrong data was inputted', data);
   	}
   });
   req.on('end', () => {
	inputData = JSON.parse(jsonData);
	XYZ_str=XYZ_str+inputData.XYZ_list;
        XYZ_str=XYZ_str.slice(1, XYZ_str.length-1);
        options.args=[inputData.HeartRate+', '+XYZ_str];
        console.log("Name: " + inputData.ID + " Sends data");
        PythonShell.run('tensorflow/Stop_Walk_Run_Classify_all.py', options, function(err,results) {
            if(err) throw err;
			console.log(inputData.ID+":"+results[0])
			if(results[0]=='0')
				res_str="정지";
			else if(results[0]=='1')
				res_str="걷기";
			else if(results[0]=='2')
				res_str="달리기";
			else if(results[0]=='3')
				res_str="아령드는모션";	
			//res.write("운동인식결과: "+res_str);
			//res.end();
			
			pool.getConnection(function (err, connection) {
			  var sql = "INSERT into history(ID, date, time, HR, class) value(?,?,?,?,?)";
			  connection.query(sql,[inputData.ID, date, time, inputData.HeartRate, results[0]], function(err,result){
				  if(err) console.error(err);
				  console.log("Insert data complete");
				  
				  connection.release();
					
			  });	 		 
				
			});
		
        });
   });
   res.write("ok");
   res.end();
 });

app.post('/login', (req, res) => {
   console.log('who get in here post /login');
   var jsonData ="";
   var id="";
   var passwd="";
   var name=""
   var inputData="";
   var result_string="";
   
   function callbackcall(function1){
	   pool.getConnection(function (err, connection) {
		  var sql = "SELECT PASSWD FROM login WHERE ID=?";
		  connection.query(sql,[id], function(err,result){
			  if(err) console.error(err);
			  
			  if(result != "")	// ID존재하는 경우
			  {
				  var DB_PW = result[0].PASSWD;
				  if(DB_PW == passwd){	// 입력한 passwd가 일치하는 경우
					  result_string = result_string+"로그인 되었습니다.";
				  }
				  else{
					  result_string = result_string+"password가 일치하지 않습니다.";
				  }
			  }
			  else{
				  result_string = result_string+"ID가 존재하지 않습니다.";
			  }
			  
			  connection.release();
			  function1();	// 인자로 받은 함수 호출
		  }); 
	   });
	}
 
   req.on('data', (data) => {
   	try{
		  jsonData += data;
   	}catch(e){
		  console.log('Wrong data was inputted', data);
   	}
   });
   
   req.on('end', () => {
	   inputData = JSON.parse(jsonData);
	   
	   id = id+inputData.ID;
	   passwd = passwd+inputData.PASSWD;
	   console.log("login ID : "+id);
	   console.log("login PW : "+passwd);
	   
	   // callback 함수 호출
	   callbackcall(function callbackfunction(){
		   res.write(result_string);
		   res.end();
	   });
	   
   });
 

   //res.write(result_string);
   //res.end();
 });

app.post('/join', (req, res) => {
   console.log('who get in here post /join');
   var jsonData ="";
   var id="";
   var passwd="";
   var name=""
   var inputData="";
   var result_string="";
   
   function callbackcall(function1){
	   pool.getConnection(function (err, connection) {
		  var sql = "SELECT idx FROM login WHERE ID=?";
		  connection.query(sql,[id], function(err,result){
			  if(err) console.error(err);
			  console.log(result);
			  connection.release();
			 
			  if(result != "")	// ID존재하는 경우
			  {
				result_string = result_string+"해당 ID가 이미 존재합니다.";
				console.log("login fail");
			  }
			  else{	
				pool.getConnection(function (err, connection) {
					var sql  = "INSERT into login(ID, PASSWD) value(?,?)";
					connection.query(sql,[id, passwd], function(err,result){
						if(err) console.error(err);
						result_string = result_string+"회원가입에 성공하였습니다.";
						connection.release();
 						function1();	// 인자로 받은 함수 호출
					});
				});
				
			  }	
			  		 
			 
		  }); 
	   });
	}
 
   req.on('data', (data) => {
   	try{
		  jsonData += data;
   	}catch(e){
		  console.log('Wrong data was inputted', data);
   	}
   });
   
   req.on('end', () => {
	   inputData = JSON.parse(jsonData);
	   
	   id = id+inputData.ID;
	   passwd = passwd+inputData.PASSWD;
	   console.log("login ID : "+id);
	   console.log("login PW : "+passwd);
	   
	   // callback 함수 호출
	   callbackcall(function callbackfunction(){
		   res.write(result_string);
		   res.end();
	   });
	   
   });
 

   //res.write(result_string);
   //res.end();
 });
 
 app.post('/main', (req, res) => {
   console.log('who get in here post /main');
   var jsonData ="";
   var id="";
   var count="";
   var inputData="";
   var result_string="";
   
   function callbackcall(function1){
	   pool.getConnection(function (err, connection) {
		  var sql = "SELECT time, HR, class FROM history WHERE ID=? ORDER BY idx DESC limit "+count;
		  connection.query(sql,[id], function(err,result){
			  if(err) console.error(err);
			  console.log(result);
			  result_string = JSON.stringify(result);
			  connection.release();
 			  function1();	// 인자로 받은 함수 호출
		 
		  }); 
	   });
	}
 
   req.on('data', (data) => {
   	try{
		  jsonData += data;
   	}catch(e){
		  console.log('Wrong data was inputted', data);
   	}
   });
   
   req.on('end', () => {
	   inputData = JSON.parse(jsonData);
	   
	   id = id+inputData.ID;
	   count = count+inputData.count;	    // callback 함수 호출
	   callbackcall(function callbackfunction(){
		   res.write(result_string);
		   res.end();
	   });
	   
   });
 
 });

app.post('/history', (req, res) => {
   var jsonData ="";
   var id="";
   var inputData="";
   var result_string="";
   function callbackcall(function1) {
       pool.getConnection(function (err, connection) {
           var sql = "SELECT class, COUNT(*) as count FROM history WHERE ID=? GROUP BY class ";
           connection.query(sql, [id], function (err, result) {
               if (err) console.error(err);
               result_string = JSON.stringify(result);
               //function1();
           });
           sql = "SELECT class as cclass, hr as hhr FROM history WHERE ID=?";
           connection.query(sql, [id], function (err, result) {
               if (err) console.error(err);
		result_string += JSON.stringify(result);
		console.log(result_string);
               connection.release();
               function1();
           });

       });
   }
   req.on('data', (data) => {
   		try{
			  jsonData += data;
		}catch(e){
			  console.log('Wrong data was inputted', data);
		}
   });

   req.on('end', () => {
	   inputData = JSON.parse(jsonData);
	   id = id+inputData.ID;
	   console.log(id + " 가 전체 운동 데이터의 조회를 원합니다.");
	   callbackcall(function callbackfunction(){
		   console.log(id+ "에게 전체 데이터 전송에 성공했습니다.");
		   res.write(result_string);
		   res.end();
	   });
   });
});
 
app.post('/historyview', (req, res) => {
   console.log('who get in here post /historyview');
   var jsonData ="";
   var id="";
   var count="";
   var Date="";
   var DateBefore="";
   var inputData="";
   var result_string="";
   
   function callbackcall(function1){
	   pool.getConnection(function (err, connection) {
		  var sql = "SELECT time, HR, class FROM history WHERE ID=? AND date between ? and ? ORDER BY idx DESC limit "+count;
		  console.log(id + " wants data from " + Date + " to  " + DateBefore);
		setTimeout(function() {
  		connection.query(sql,[id,DateBefore,Date], function(err,result){
			  if(err) console.error(err);
			  console.log(result);
			  result_string = JSON.stringify(result);
			  connection.release();
 			  function1();	// 인자로 받은 함수 호출
		 
		  }); 
		}, 1000);
		  
	   });
	}
 
   req.on('data', (data) => {
   	try{
		  jsonData += data;
   	}catch(e){
		  console.log('Wrong data was inputted', data);
   	}
   });
   
   req.on('end', () => {
	   inputData = JSON.parse(jsonData);
	   
	   id = id+inputData.ID;
	   count = count+inputData.count;
	   Date = Date+inputData.Date;
	   DateBefore=DateBefore+inputData.DateBefore;
	    // callback 함수 호출
	   callbackcall(function callbackfunction(){
		   res.write(result_string);
		   console.log("Success fully sends data of " + Date + " to " + id);
		   res.end();
	   });
	   
   });
 
 });


app.listen(3000, () => {
  console.log('Example app listening on port 3000!');
});
