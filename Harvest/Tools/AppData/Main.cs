using System;
using System.IO;
using System.Collections.Generic;
using System.Web.Script.Serialization;

public class Ilr
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
        List<TrainingData> Sources = new List<TrainingData>();
        var Entries = File.ReadAllLines("../../Data/Final/Data.json");
        var deserializer = new JavaScriptSerializer();
        foreach(var Entry in Entries)
        {
            TrainingData Data = new TrainingData();
            Sources.Add( deserializer.Deserialize<TrainingData>(Entry) );
        }

        Int32 Count = 0;
        const string BaseDirectory = "Data";
        Directory.CreateDirectory(BaseDirectory);

        while(Sources.Count > 0)
        {
            string CurrentFile = BaseDirectory + Count;

            File.AppendAllText(BaseDirectory + "/" + CurrentFile, "[" + Environment.NewLine);

            Int32 RecordCount = 12000; // Around 2MB
            while(Sources.Count > 1 && RecordCount-- > 1)
            {
                Int32 Ndx = Sources.Count-1;
                File.AppendAllText(BaseDirectory + "/" + CurrentFile, deserializer.Serialize(Sources[Ndx]) + "," + Environment.NewLine);
                Sources.RemoveAt(Ndx);
            }

            Int32 Final = Sources.Count-1;
            File.AppendAllText(BaseDirectory + "/" + CurrentFile, deserializer.Serialize(Sources[Final]) + Environment.NewLine);
            Sources.RemoveAt(Final);

            File.AppendAllText(BaseDirectory + "/" + CurrentFile, "]" + Environment.NewLine);

            Count++;
        }

        return 0;
    }
}
