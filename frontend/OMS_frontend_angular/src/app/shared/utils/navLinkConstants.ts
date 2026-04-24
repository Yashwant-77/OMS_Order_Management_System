export const navLinkConstants: any =  {
    ApiEndPoints : {} ,
    pattern : {} , 
    links : [
        {
            path : "/users",
            text : "Users",
            icon : "",
            role : ["administrator"]

        },
        {
            path : "/orders",
            text : "Orders",
            icon : "",
            role : ["administrator, salesRepresentative"]

        },
        {
            path : "/bom",
            text : "BOM",
            icon : "",
            role : ["administrator , productManager"]

        },
        {
            path : "/purchase",
            text : "Purchase",
            icon : "",
            role : ["administrator , purchasingManager"]

        },
        {
            path : "/invoice",
            text : "Invoice",
            icon : "",
            role : ["administrator , financeManager"]

        },
        {
            path : "/reports",
            text : "Reports",
            icon : "",
            role : ["administrator , businessAnalytics"]

        },
       
       
    ]
}