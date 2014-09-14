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
    
    $scope.findAll();
    
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
    
     $scope.delete = function (requestType) {
        RequestTypeFacade.delete(requestType).success(function () {
            var index = $scope.requestTypes.indexOf(requestType);

            if (index > -1) {
                $scope.requestTypes.splice(index, 1);
            }
        }).error(function (data, status) {

            console.log("Ошибка при удалении типа запроса: " + requestType.name + ". Status: " + status);

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
            url: "./webresources/request_type"
        });
    };
    
    requestTypeFacade.create = function (requestType) {
        return $http({
            method: "POST",
            data: requestType,
            withCredentials: true,
            url: "./webresources/request_type"
        });
    };
    
    requestTypeFacade.delete = function (requestType) {
        return $http({
            method: "DELETE",
            withCredentials: true,
            url: "./webresources/request_type/"+requestType.id
        });
    };
    
    return requestTypeFacade;
});