using System;
using System.IO;
using System.Drawing;
using System.Drawing.Imaging;
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

//Left Right Top Bottom
//30.560331 29.981509 -97.938686 -97.572487

//578822 358008
//0.001865 0.005642  

    public static int Main(String[] args)
    {
        List<TrainingData> Sources = new List<TrainingData>();
        var Entries = File.ReadAllLines("../../Data/Final/Data.json");
//        var Entries = File.ReadAllLines("../../Data/Final/VoidData.json");
        var deserializer = new JavaScriptSerializer();
        foreach(var Entry in Entries)
        {
            Sources.Add( deserializer.Deserialize<TrainingData>(Entry) );
        }

        const Int32 Scale = 1000000;
        const Int32 ScreenWidth = 1080;
        const Int32 ScreenHeight = 2020;

        const float WidthScale = 0.00207f;
        const float HeightScale = 0.0065f;
        const float LeftCoordinate = 30.560331f;
        const float TopCoordinate = -97.938686f;

        Int32 Count = 512;
        Random Choice = new Random((Int32)DateTime.Now.ToFileTimeUtc());
        List<Int32> Chosen = new List<Int32>();     
        while(Count-- > 0)
        {
            Chosen.Add(Choice.Next(0, Sources.Count));
        }

        Int32 Ndx = 0;
        Int32 OutOfBounds = 0;
        Bitmap Map = new Bitmap("Plain.png");
        foreach(var Source in Sources)
        {
            var Width = LeftCoordinate*Scale - Source.Latitude*Scale;
            var Height = Math.Abs(TopCoordinate*Scale) - Math.Abs(Source.Longitude*Scale);
            Int32 X = (Int32)(ScreenWidth - Math.Ceiling(Width*WidthScale));
            Int32 Y = (Int32)(ScreenHeight - Math.Ceiling(Height*HeightScale));
            if((X >=0 && X < ScreenWidth) && (Y >= 0 && Y < ScreenHeight))
            {
                if(Chosen.Contains(Ndx))
//                if(Source.Street.Contains("1408 JUSTIN") || Source.Street.Contains("12469 BRODIE")) // 630 760
                {
                    Color Category = Color.LightBlue;
                    if(Source.Rainfall >= 13.0f)
                    {
                        Category = Color.Lime;
                    }
                    if(Source.Rainfall >= 16.0f)
                    {
                        Category = Color.Orange;
                    }
                    if(Source.Rainfall >= 19.0f)
                    {
                        Category = Color.Red;
                    }
                    if(Source.Rainfall >= 22.0f)
                    {
                        Category = Color.Purple;
                    }

                    Category = Color.FromArgb(26, Category);
//                    Map.SetPixel(X, Y, Category);
                    const Int32 Diameter = 100;
                    Graphics g = Graphics.FromImage(Map);
                    g.FillEllipse(new SolidBrush(Category), X, Y, Diameter, Diameter);
                }
            }
            else
            {
                Console.WriteLine(++OutOfBounds);
            }
            Ndx++;
        }   
        Map.Save("Test.png", ImageFormat.Png); 

        return 0;
    }
}
