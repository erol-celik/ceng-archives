using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace _2022510164.erol.celik
{
    internal class Program
    {
        static void Main(string[] args)
        {
            int numdup = 100;
            int numdown = 3, resultdown = 0;
            double resultup = 1.0, result = 0.0, resulttotal = 0;
            for (int j = 1; j <= 12; j++)
            {
                resultup = Math.Sqrt(numdup * resultup);
                for (int i = 1; i <= (j * 2); i++)
                {
                    resultdown = resultdown + numdown;
                    numdown = numdown + 2;
                    result = resultup / resultdown;
                }
                numdup = numdup - 5;
                numdown = 3;
                numdown = numdown + j;
                resultdown = 0;
                resulttotal = resulttotal + result;
            }
            Console.WriteLine(resulttotal);
            Console.ReadLine();
        }
    }
}
