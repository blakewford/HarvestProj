using System;
using System.IO;
using System.Threading;
using System.Collections.Generic;
using System.Web.Script.Serialization;

public class Pipeline
{
    private class GaugeData
    {
        public string Name = String.Empty;
        public float Latitude = 0.0f;
        public float Longitude = 0.0f;
        public Int32 MeterId = -1; 
        public float RainfallTotal = 0.0f;  
    };
    
    private class TrainingData
    {
        public string Street = String.Empty;
        public float Latitude = 0.0f;
        public float Longitude = 0.0f;
        public Int32 MeterId = -1;
        public Int32 MeterDistance = -1;
        public float Rainfall = 0.0f;
        public float Impervious = 0.0f;
        public float Area = 0.0f;     
    };

    private static List<GaugeData> Gauges = new List<GaugeData>();  
    private static List<TrainingData> Sources = new List<TrainingData>();    
    private static List<TrainingData> VoidSources = new List<TrainingData>();

    private static void BuildGauges(string[] Raw)
    {
        foreach(var Gauge in Raw)
        {
            var Components = Gauge.Split(',');

            GaugeData Data = new GaugeData();
            Data.MeterId = Int32.Parse(Components[0]);
            Data.Name = Components[1];
            Data.Latitude = float.Parse(Components[2]);
            Data.Longitude = float.Parse(Components[3]);

            var Lines = File.ReadAllLines("../../Data/Rainfall/Raw/"+Components[1]+"_rain.csv");

            float Total = 0;
            Int32 Count = Lines.Length;
            while(Count-- > 1)
            {
                var Parts = Lines[Count].Split(',');
                Total += float.Parse(Parts[2]);
            }

            Data.RainfallTotal = Total;
            Gauges.Add(Data);            
        }
    }

    private static void BuildAddressList(string[] Raw)
    {
        foreach(string Address in Raw)
        {
            TrainingData Data = new TrainingData();
    
            var Components = Address.Split(',');
            Data.Street = Components[0];
            var Coordinate = Components[1].Split(' ');
    
            Data.Latitude = float.Parse(Coordinate[1]);
            Data.Longitude = float.Parse(Coordinate[0]);
    
            Int32 Id = 0;
            float Rainfall = 0.0f;
            Int32 Shortest = Int32.MaxValue;
            foreach(var Gauge in Gauges)
            {
                var Temp = CalculateDistance(Data.Latitude, Data.Longitude, Gauge.Latitude, Gauge.Longitude);
                if(Temp < Shortest && Gauge.MeterId != 2300)
                {
                    Shortest = Temp;
                    Id = Gauge.MeterId;
                    Rainfall = Gauge.RainfallTotal;
                }
            }

            Data.MeterId = Id;
            Data.MeterDistance = Shortest;
            Data.Rainfall = Rainfall;
            Sources.Add(Data);
        }        
    } 

    private static void MatchImperviousCover(string[] Raw)
    {
        List<Thread> MatchingThreads = new List<Thread>();
        List<TrainingData> Matched = new List<TrainingData>();

        Int32 Count = Environment.ProcessorCount*2;
        Int32 Chunk = (Raw.Length / Count);
        Int32 LeftOver = (Raw.Length % Count);
        for(Int32 i = 0; i < Count; i++)
        {
            Int32 Size = Chunk;            
            Int32 Start = i*Chunk;
            string[] SubArray = new string[Size];
            if(i == Count-1)
            {
                Size = Size + LeftOver;
                SubArray = new string[Size];
            }
            Array.Copy(Raw, Start, SubArray, 0, Size);
            MatchingThreads.Add(new Thread(() => {
                Int32 Found = 0;               
                List<TrainingData> Copy = new List<TrainingData>(Sources.ToArray());
                List<TrainingData> SubMatched = new List<TrainingData>();                 
                foreach(var Entry in SubArray)
                {
                    var Parts = Entry.Split(',');
                    foreach(var Source in Copy)
                    {
                        if(Source.Street.Contains(Parts[0]))
                        {
                            Found++;
                            bool First = float.TryParse(Parts[1].Trim(), out Source.Impervious);
                            bool Second = float.TryParse(Parts[2].Trim(), out Source.Area);
                            if(First && Second && Source.Impervious > 0 && Source.Area > 0 && Source.Area < 20000 && Source.Area > 1000)
                            {
                                SubMatched.Add(Source);
                            }
                            else
                            {
//                                Console.WriteLine(Parts[1] +" "+ Parts[2]);
                            }
                            Copy.Remove(Source);                                                   
                            break;
                        }
                    }
                } 

                lock(Matched)
                {                                    
                    Matched.AddRange(SubMatched);                                  
                }
                lock(VoidSources)
                {
                    foreach(var Match in SubMatched)
                    {
                        VoidSources.Remove(Match);
                    }
                }                              
            }));
        }

        foreach(var Thread in MatchingThreads)
        {
            Thread.Start();
        }
        foreach(var Thread in MatchingThreads)
        {
            Thread.Join();
        } 

        Console.WriteLine(DateTime.Now + ": " +Matched.Count);

        const string FileName = "Data.json";
        File.Delete(FileName);

        JavaScriptSerializer serializer = new JavaScriptSerializer();
        foreach(var Address in Matched)
        {
            File.AppendAllText(FileName, serializer.Serialize(Address) + "\n");
        }

        const string VoidFileName = "VoidData.json";
        File.Delete(VoidFileName);        
        foreach(var Address in VoidSources)
        {
            File.AppendAllText(VoidFileName, serializer.Serialize(Address) + "\n");
        }        
    }

    public static int Main(String[] args)
    {
        Console.WriteLine("Working with " + Environment.ProcessorCount + " CPUs");

        BuildGauges(File.ReadAllLines("../../Data/Lookup/Locations.csv"));
        BuildAddressList(File.ReadAllLines("../../Data/Address/Managable.log"));

        VoidSources.AddRange(Sources);
        MatchImperviousCover(File.ReadAllLines("../../Data/Coverage/ilr.csv"));

        return 0;
    }

    private static double toRadians(double degrees)
    {
        var pi = Math.PI;
        return degrees * (pi/180);
    }

    private static Int32 CalculateDistance(float lat1, float lon1, float lat2, float lon2)
    {
        var R = 6371e3; // meters
       
       
       var φ1 = toRadians(lat1);
       var φ2 = toRadians(lat2);
       var Δφ = toRadians(lat2-lat1);
       var Δλ = toRadians(lon2-lon1);
       
       var a = Math.Sin(Δφ/2) * Math.Sin(Δφ/2) +
               Math.Cos(φ1) * Math.Cos(φ2) *
               Math.Sin(Δλ/2) * Math.Sin(Δλ/2);
       var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1-a));
       
       var d = R * c;
       
       // meters
       return Convert.ToInt32(Math.Round(d));       
    }     
}
