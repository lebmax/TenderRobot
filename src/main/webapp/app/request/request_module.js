var requestModule = angular.module('requestModule', []);

requestModule.controller("RequestCtrl", function ($scope, RequestType, RequestTypeFacade, $location) {
    $scope.requestType = RequestType;
    
    $scope.findAll = function(){
        RequestTypeFacade.findAll()
        .success(function(data){
            $scope.requestTypes = data;
        }).error(function(data, status){
            console.log("Requests types not found. Error: "+data+" Status: "+status);
        });
    };
    
    $scope.create = function(){
        console.log($scope.requestType);
        RequestTypeFacade.create($scope.requestType)
        .success(function(data){
            console.log("Request type was saved.");
            $location.path("/task");
        }).error(function(data, status){
            console.log("Error saving request type. Error: "+data+" Status: "+status);
        });
    };
});

requestModule.factory('RequestType', function () {
    var requestType = {
        id: null,
        name: null
    };
    return requestType;
});

requestModule.factory('RequestTypeFacade', function ($http) {
    var requestTypeFacade = {};
    
    requestTypeFacade.findAll = function () {
        return $http({
            method: "GET",
            withCredentials: true,
            url: "http://localhost:8080/TenderRobot/webresources/request_type"
        });
    };
    
    requestTypeFacade.create = function (request) {
        return $http({
            method: "POST",
            data: request,
            withCredentials: true,
            url: "http://localhost:8080/TenderRobot/webresources/request_type"
        });
    };
    
    
    return requestTypeFacade;
});