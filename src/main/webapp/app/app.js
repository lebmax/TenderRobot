var app = angular.module('tenderRobot', [
    'mainModule', 
    'taskModule', 
    'taskStatusModule',
    'ngRoute']);

app.config(function($routeProvider) {
    $routeProvider.
    when('/', {
        templateUrl: "app/index/index.tpl.html"
    }).when('/task', {
        templateUrl: "app/task/task.tpl.html"
    }).when('/auction', {
        templateUrl: "app/auction/auction.tpl.html"
    }).when('/task_status', {
        templateUrl: "app/task_status/task_status.tpl.html"
    }).when('/request', {
        templateUrl: "app/request/request.tpl.html"
    });
});