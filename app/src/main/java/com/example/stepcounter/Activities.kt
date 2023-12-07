package com.example.stepcounter

class Activities {
    private val activityList= mutableListOf<String>()

    public fun addActivity(a:String){
        activityList.add(a)
    }

    public fun getActivities():MutableList<String>{
        return activityList
    }
}