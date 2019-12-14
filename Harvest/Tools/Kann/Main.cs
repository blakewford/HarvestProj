using System;
using System.IO;
using System.Collections.Generic;
using System.Web.Script.Serialization;

public class Snake
{
    private class TrainingData
    {
        public string Street;
        public float Latitude;
        public float Longitude;
        public Int32 MeterId;
        public Int32 MeterDistance;
        public float Rainfall;
        public float Impervious;
        public float Area;     
    };

    public static int Main(String[] args)
    {
        const Int32 Multiplier = 10000;
        var Entries = File.ReadAllLines("../Pipeline/Data.json");
        var deserializer = new JavaScriptSerializer();
        foreach(var Entry in Entries)
        {
            var Data = deserializer.Deserialize<TrainingData>(Entry);
            Console.WriteLine(String.Format("{0:0} {1:0} {2:0}", Data.Latitude*Multiplier, Math.Abs(Data.Longitude)*Multiplier, Data.Impervious*Multiplier));
        }                

        return 0;
    }   
}
